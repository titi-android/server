package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.bus.Bus;
import com.example.busnotice.domain.bus.BusRepository;
import com.example.busnotice.domain.bus.BusService;
import com.example.busnotice.domain.bus.res.BusStationArriveResponse.Item;
import com.example.busnotice.domain.busStop.BusStop;
import com.example.busnotice.domain.busStop.BusStopRepository;
import com.example.busnotice.domain.busStop.BusStopService;
import com.example.busnotice.domain.schedule.req.CreateScheduleRequest;
import com.example.busnotice.domain.schedule.req.UpdateScheduleRequest;
import com.example.busnotice.domain.schedule.res.ScheduleResponse;
import com.example.busnotice.domain.schedule.res.ScheduleResponses;
import com.example.busnotice.domain.schedule.res.ScheduleResponses.BusInfoDto;
import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.user.UserRepository;
import com.example.busnotice.global.code.StatusCode;
import com.example.busnotice.global.exception.ScheduleException;
import com.example.busnotice.global.exception.UserException;
import com.example.busnotice.global.jwt.JwtProvider;
import com.example.busnotice.util.DayConverter;
import java.io.UnsupportedEncodingException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final JwtProvider jwtProvider;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final BusStopRepository busStopRepository;
    private final BusRepository busRepository;
    private final BusService busService;
    private final BusStopService busStopService;

    @Transactional
    public void createSchedule(Long userId, CreateScheduleRequest createScheduleRequest)
        throws UnsupportedEncodingException {
        User user = getUserById(userId);

        // 겹치는 스케줄 있는지 확인
        새_스케줄_생성시_겹침_유무_파악(user, createScheduleRequest.days(), createScheduleRequest.startTime(),
            createScheduleRequest.endTime());

        // 도시 코드
        String cityCode = busStopService.도시코드_조회(createScheduleRequest.regionName());
        // 스케줄상의 버스 정류장의 node id
        String nodeId = busService.버스정류장_노드_ID_조회(cityCode, createScheduleRequest.busStopName());
        // 스케줄상의 버스 정류장 생성
        BusStop busStop = BusStop.toEntity(cityCode, createScheduleRequest.busStopName(), nodeId);
        busStopRepository.save(busStop);
        // 해당 스케줄상의 버스 정류장에 버스 목록 등록
        List<String> busNames = createScheduleRequest.busList();
        List<Bus> buses = busNames.stream().map(busName -> Bus.toEntity(busStop, busName)).toList();
        busRepository.saveAll(buses);
        // 스케줄 생성 후 생성한 버스 정류장 등록
        Schedule schedule = Schedule.toEntity(user, createScheduleRequest.name(),
            createScheduleRequest.days(),
            createScheduleRequest.startTime(), createScheduleRequest.endTime(), busStop);
        scheduleRepository.save(schedule);
    }

    @Transactional
    public void updateSchedule(Long userId, Long scheduleId,
        UpdateScheduleRequest updateScheduleRequest)
        throws UnsupportedEncodingException {
        User user = getUserById(userId);
        Schedule existSchedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new ScheduleException(
                StatusCode.NOT_FOUND, "해당 스케줄이 존재하지 않습니다."));

        // 수정 전 스케줄을 제외하고, 수정한 스케줄과 겹치는 스케줄 있는지 확인
        기존_스케줄_수정시_겹침_유무_파악(user, scheduleId, updateScheduleRequest.days(),
            updateScheduleRequest.startTime(),
            updateScheduleRequest.endTime());

        // 도시 코드
        String cityCode = busStopService.도시코드_조회(updateScheduleRequest.regionName());
        // 수정한 스케줄상의 버스 정류장의 node id
        String newNodeId = busService.버스정류장_노드_ID_조회(cityCode, updateScheduleRequest.busStopName());
        // 스케줄상의 기존 버스 정류장 엔티티
        BusStop existBusStop = busStopRepository.findById(existSchedule.getBusStop().getId()).get();
        // 해당 버스 정류장에 등록된 버스들 삭제
        List<Long> busIds = existBusStop.getBusList().stream().map(bus -> bus.getId()).toList();
        busRepository.deleteAllById(busIds);
        // 버스 정류장 업데이트 및 버스 생성
        List<String> busNames = updateScheduleRequest.busList();
        List<Bus> newBuses = busNames.stream().map(busName -> Bus.toEntity(existBusStop, busName))
            .toList();
        busRepository.saveAll(newBuses);
        existBusStop.update(cityCode, updateScheduleRequest.busStopName(), newNodeId, newBuses);
        // 최종적으로 스케줄 업데이트
        existSchedule.update(
            updateScheduleRequest.name(),
            updateScheduleRequest.days(),
            updateScheduleRequest.startTime(),
            updateScheduleRequest.endTime(),
            existBusStop);
    }

    @Transactional
    public void deleteSchedule(Long userId, Long scheduleId) {
        User user = getUserById(userId);
        Schedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new ScheduleException(StatusCode.NOT_FOUND, "삭제할 스케줄이 존재하지 않습니다."));
        if (schedule.getUser().getId() != user.getId()) {
            throw new ScheduleException(StatusCode.BAD_REQUEST, "삭제할 스케줄이 본인의 스케줄이 아닙니다.");
        }
        scheduleRepository.deleteById(scheduleId);
    }

    public ScheduleResponse 현재_스케줄의_가장_빨리_도착하는_버스_정보(Long userId)
        throws UnsupportedEncodingException {
        User user = getUserById(userId);
        // 현재 스케줄
        Schedule currentSchedule = getCurrentSchedule(user);
        // 현재 스케줄의 버스정류장
        BusStop busStop = currentSchedule.getBusStop();
        // 현재 스케줄의 버스정류장에 등록된 버스들
        List<String> busNames = busStop.getBusList().stream().map(bus -> bus.getName()).toList();
        Item fastestBus = busService.특정_노드_ID에_가장_빨리_도착하는_버스_조회(busStop.getCityCode(),
            busStop.getNodeId(), busNames);
        return fastestBus.toScheduleResponse(currentSchedule.getDays(),
            currentSchedule.getStartTime(),
            currentSchedule.getEndTime());
    }

    public ScheduleResponses 현재_스케줄의_가장_빨리_도착하는_첫번째_두번째_버스_정보(Long userId)
        throws UnsupportedEncodingException {
        User user = getUserById(userId);
        // 현재 스케줄
        Schedule currentSchedule = getCurrentSchedule(user);
        // 현재 스케줄의 버스정류장
        BusStop busStop = currentSchedule.getBusStop();
        // 현재 스케줄의 버스정류장에 등록된 버스들
        List<String> busNames = busStop.getBusList().stream().map(bus -> bus.getName()).toList();
        List<Item> items = busService.특정_노드_ID에_가장_빨리_도착하는_첫번째_두번째_버스_조회(busStop.getCityCode(),
            busStop.getNodeId(),
            busNames);
        // 버스 도착 정보만 배열로 따로 빼서 오름차순 정렬
        List<BusInfoDto> busInfoDtos = items.stream().map(
            item -> item.toBusInfoDto(item.getArrprevstationcnt(), item.getArrtime(),
                item.getNodeid(), item.getNodenm(), item.getRouteid(), item.getRouteno(),
                item.getRoutetp(), item.getVehicletp())
        ).sorted(Comparator.comparing(BusInfoDto::arrtime)).toList();
        // 요일과 시간대는 필드에 직접 주입
        return new ScheduleResponses(currentSchedule.getName(), currentSchedule.getDays(), currentSchedule.getStartTime(),
            currentSchedule.getEndTime()
            , busInfoDtos);
    }

    public List<ScheduleResponse> 오늘_스케줄들의_가장_빨리_도착하는_버스_정보(Long userId)
        throws UnsupportedEncodingException {
        User user = getUserById(userId);
        String today = DayConverter.getTodayAsString();
        List<Schedule> schedules = scheduleRepository.findAllByUserAndDays(user, today)
            .orElseThrow(() -> new ScheduleException(StatusCode.NO_CONTENT, "오늘의 스케줄이 존재하지 않습니다."));

        List<ScheduleResponse> scheduleResponses = new ArrayList<>();
        for (Schedule s : schedules) {
            List<String> busNames = s.getBusStop().getBusList().stream()
                .map(bus -> bus.getName()).toList();
            Item item = busService.특정_노드_ID에_가장_빨리_도착하는_버스_조회(s.getBusStop().getCityCode(),
                s.getBusStop().getNodeId(), busNames);
            scheduleResponses.add(
                item.toScheduleResponse(s.getDays(), s.getStartTime(), s.getEndTime()));
        }
        return scheduleResponses.stream().sorted(Comparator.comparing(ScheduleResponse::startTime))
            .toList();
    }

    public List<ScheduleResponses> 오늘_스케줄들의_가장_빨리_도착하는_첫번째_두번째_버스_정보(Long userId)
        throws UnsupportedEncodingException {
        User user = getUserById(userId);
        String today = DayConverter.getTodayAsString();
        List<Schedule> schedules = scheduleRepository.findAllByUserAndDays(user, today)
            .orElseThrow(() -> new ScheduleException(StatusCode.NO_CONTENT, "오늘의 스케줄이 존재하지 않습니다."));

        List<ScheduleResponses> scheduleResponsesList = new ArrayList<>();
        for (Schedule s : schedules) {
            List<String> busNames = s.getBusStop().getBusList().stream()
                .map(bus -> bus.getName()).toList();
            List<Item> items = busService.특정_노드_ID에_가장_빨리_도착하는_첫번째_두번째_버스_조회(
                s.getBusStop().getCityCode(),
                s.getBusStop().getNodeId(), busNames);

            // 버스 도착 정보만 배열로 따로 빼서 오름차순 정렬
            List<BusInfoDto> busInfoDtos = items.stream().map(
                item -> item.toBusInfoDto(item.getArrprevstationcnt(), item.getArrtime(),
                    item.getNodeid(), item.getNodenm(), item.getRouteid(), item.getRouteno(),
                    item.getRoutetp(), item.getVehicletp())
            ).sorted(Comparator.comparing(BusInfoDto::arrtime)).toList();
            ScheduleResponses scheduleResponses = new ScheduleResponses(s.getName(), s.getDays(),
                s.getStartTime(), s.getEndTime()
                , busInfoDtos);
            scheduleResponsesList.add(scheduleResponses);
        }
        return scheduleResponsesList.stream().sorted(
            Comparator.comparing(responses -> responses.startTime())).toList();
    }

    private void 새_스케줄_생성시_겹침_유무_파악(User user, String days, LocalTime startTime,
        LocalTime endTime) {
        List<Schedule> schedules = scheduleRepository.findAllByUser(user)
            .orElseThrow(() -> new ScheduleException(StatusCode.NO_CONTENT, "유저의 스케줄이 존재하지 않습니다."));

        for (Schedule s : schedules) {
            if (요일_겹침_유무(s.getDays(), days) && 시간대_겹침_유무(startTime, endTime, s.getStartTime(),
                s.getEndTime())) {
                throw new ScheduleException(StatusCode.CONFLICT, "스케줄의 요일과 시간대가 겹칩니다.");
            }
        }
    }

    private void 기존_스케줄_수정시_겹침_유무_파악(User user, Long scheduleId, String days,
        LocalTime startTime,
        LocalTime endTime) {
        List<Schedule> schedules = scheduleRepository.findAllByUser(user)
            .orElseThrow(() -> new ScheduleException(StatusCode.NO_CONTENT, "유저의 스케줄이 존재하지 않습니다."));

        for (Schedule es : schedules) {
            if (scheduleId != es.getId()
                && 요일_겹침_유무(days, es.getDays())
                && 시간대_겹침_유무(startTime, endTime, es.getStartTime(), es.getEndTime())) {
                throw new ScheduleException(StatusCode.CONFLICT, "스케줄의 요일과 시간대가 겹칩니다.");
            }
        }
    }

    private boolean 시간대_겹침_유무(LocalTime startTime, LocalTime endTime,
        LocalTime existStartTime, LocalTime existEndTime) {
        return (startTime.isBefore(existEndTime) && endTime.isAfter(existStartTime));
    }


    private boolean 요일_겹침_유무(String newDays, String existDays) {
        if (newDays.equals(existDays)) {
            return true;
        }
        return false;
    }

    public Schedule getCurrentSchedule(User user) {
        String today = DayConverter.getTodayAsString();
        Schedule schedule = scheduleRepository.findByCurrentTimeAndDay(user,
                today, LocalTime.now())
            .orElseThrow(() -> new ScheduleException(StatusCode.NO_CONTENT, "현재 스케줄이 존재하지 않습니다."));
        return schedule;
    }

    private User getUserByBearerToken(String bearerToken) {
        String token = jwtProvider.extractToken(bearerToken);
        String username = jwtProvider.getUsername(token);
        User user = userRepository.findByName(username)
            .orElseThrow(() -> new UserException(StatusCode.NOT_FOUND, "유저가 존재하지 않습니다."));
        return user;
    }

    private User getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
            () -> new UserException(StatusCode.NOT_FOUND, "해당 ID의 유저가 존재하지 않습니다.")
        );
        return user;
    }
}