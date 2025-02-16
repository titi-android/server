package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.bus.Bus;
import com.example.busnotice.domain.bus.BusRepository;
import com.example.busnotice.domain.bus.BusService;
import com.example.busnotice.domain.bus.res.BusArrInfosDto.Item;
import com.example.busnotice.domain.busStop.BusStop;
import com.example.busnotice.domain.busStop.BusStopRepository;
import com.example.busnotice.domain.busStop.BusStopService;
import com.example.busnotice.domain.schedule.repository.ScheduleRepository;
import com.example.busnotice.domain.schedule.req.CreateScheduleRequest;
import com.example.busnotice.domain.schedule.req.UpdateScheduleRequest;
import com.example.busnotice.domain.schedule.res.ScheduleInfoResponse;
import com.example.busnotice.domain.schedule.res.ScheduleResponse;
import com.example.busnotice.domain.schedule.res.ScheduleResponses;
import com.example.busnotice.domain.schedule.res.ScheduleResponses.BusInfoDto;
import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.user.UserRepository;
import com.example.busnotice.global.code.ErrorCode;
import com.example.busnotice.global.code.StatusCode;
import com.example.busnotice.global.exception.ScheduleException;
import com.example.busnotice.global.exception.UserException;
import com.example.busnotice.util.DayConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final BusStopRepository busStopRepository;
    private final BusRepository busRepository;
    private final BusService busService;
    private final BusStopService busStopService;

    @Transactional
    public void createSchedule(Long userId, CreateScheduleRequest createScheduleRequest) {
        User user = getUserById(userId);

        // 겹치는 스케줄 있는지 확인
        새_스케줄_생성시_겹침_유무_파악(user, createScheduleRequest.daysList(),
            createScheduleRequest.startTime(), createScheduleRequest.endTime());

        // 도시 코드
        String cityCode = busStopService.도시코드_DB_조회(createScheduleRequest.regionName());
        // 스케줄상의 버스 정류장의 node id
        String nodeId = createScheduleRequest.nodeId();
        // 스케줄상의 버스 정류장 생성
        BusStop busStop = BusStop.toEntity(cityCode, createScheduleRequest.busStopName(), nodeId);
        busStopRepository.save(busStop);
        // 해당 스케줄상의 버스 정류장에 버스 목록 등록
        List<CreateScheduleRequest.BusInfo> busInfos = createScheduleRequest.busInfos();
        List<Bus> buses = busInfos.stream()
            .map(busInfo -> Bus.toEntity(busStop, busInfo.name(), busInfo.type())).toList();
        busRepository.saveAll(buses);
        // 스케줄 생성 후 생성한 버스 정류장 등록
        Schedule schedule = Schedule.toEntity(user, createScheduleRequest.name(),
            createScheduleRequest.daysList(), createScheduleRequest.regionName(),
            createScheduleRequest.startTime(), createScheduleRequest.endTime(), busStop,
            createScheduleRequest.isAlarmOn());
        Schedule savedSchedule = scheduleRepository.save(schedule);
        log.info("savedSchedule.toString(): {}", savedSchedule);
    }

    public ScheduleInfoResponse getSchedule(Long userId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findByIdAndUserId(scheduleId, userId)
            .orElseThrow(() -> new ScheduleException(ErrorCode.SCHEDULE_NOT_FOUND));
        return ScheduleInfoResponse.fromEntity(schedule);
    }

    @Transactional
    public void updateSchedule(Long userId, Long scheduleId,
        UpdateScheduleRequest updateScheduleRequest) throws IOException {
        User user = getUserById(userId);
        Schedule existSchedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new ScheduleException(ErrorCode.SCHEDULE_NOT_FOUND));

        // 수정 전 스케줄을 제외하고, 수정한 스케줄과 겹치는 스케줄 있는지 확인
        기존_스케줄_수정시_겹침_유무_파악(user, scheduleId, updateScheduleRequest.daysList(),
            updateScheduleRequest.startTime(), updateScheduleRequest.endTime());

        // 도시 코드
        String cityCode = busStopService.도시코드_DB_조회(updateScheduleRequest.regionName());
        // 수정한 스케줄상의 버스 정류장의 node id
        String newNodeId = busStopService.버스정류장_노드_ID_조회(updateScheduleRequest.regionName(),
            updateScheduleRequest.busStopName());
        // 스케줄상의 기존 버스 정류장 엔티티
        BusStop existBusStop = existSchedule.getBusStop();
        // 해당 버스 정류장에 등록된 버스들 삭제
        List<Long> busIds = existBusStop.getBusList().stream().map(bus -> bus.getId()).toList();
        busRepository.deleteAllById(busIds);
        // 버스 정류장 업데이트 및 버스 생성
        List<UpdateScheduleRequest.BusInfo> busInfos = updateScheduleRequest.busInfos();
        List<Bus> newBuses = busInfos.stream()
            .map(busInfo -> Bus.toEntity(existBusStop, busInfo.name(),
                busInfo.type()))
            .toList();
        busRepository.saveAll(newBuses);
        existBusStop.update(cityCode, updateScheduleRequest.busStopName(), newNodeId, newBuses);
        // 최종적으로 스케줄 업데이트
        existSchedule.update(updateScheduleRequest.name(), updateScheduleRequest.daysList(),
            updateScheduleRequest.startTime(), updateScheduleRequest.endTime(), existBusStop,
            updateScheduleRequest.isAlarmOn());
    }

    @Transactional
    public void deleteSchedule(Long userId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new ScheduleException(ErrorCode.SCHEDULE_NOT_FOUND));

        scheduleRepository.deleteById(scheduleId);
    }

    public ScheduleResponse 현재_스케줄의_가장_빨리_도착하는_버스_정보(Long userId)
        throws UnsupportedEncodingException {
        User user = getUserById(userId);
        // 현재 스케줄
        Optional<Schedule> optionalCurrentSchedule = getCurrentSchedule(user);
        if (optionalCurrentSchedule.isEmpty()) {
            return null;
        }
        Schedule currentSchedule = optionalCurrentSchedule.get();
        // 현재 스케줄의 버스정류장
        BusStop busStop = currentSchedule.getBusStop();
        // 현재 스케줄의 버스정류장에 등록된 버스들
        List<String> busNames = getBusNames(busStop);
        Item fastestBus = busService.특정_노드_ID에_가장_빨리_도착하는_버스_조회(currentSchedule.getRegionName(),
            busStop.getNodeId(), busNames);
        if (fastestBus == null) {
            return new ScheduleResponse(currentSchedule.getId(), currentSchedule.getName(),
                currentSchedule.getDaysList(), currentSchedule.getStartTime(),
                currentSchedule.getEndTime(), null, currentSchedule.getIsAlarmOn());
        }
        return fastestBus.toScheduleResponse(currentSchedule.getId(), currentSchedule.getName(),
            currentSchedule.getDaysList(), currentSchedule.getStartTime(),
            currentSchedule.getEndTime(), currentSchedule.getIsAlarmOn());
    }

    public ScheduleResponses 현재_스케줄의_가장_빨리_도착하는_첫번째_두번째_버스_정보(Long userId)
        throws UnsupportedEncodingException {
        User user = getUserById(userId);
        // 현재 스케줄
        Optional<Schedule> optionalCurrentSchedule = getCurrentSchedule(user);
        if (optionalCurrentSchedule.isEmpty()) {
            return null;
        }
        Schedule currentSchedule = optionalCurrentSchedule.get();
        // 현재 스케줄의 버스정류장
        BusStop busStop = currentSchedule.getBusStop();
        // 현재 스케줄의 버스정류장에 등록된 버스들
        List<String> busNames = getBusNames(busStop);
        List<Item> items = busService.특정_노드_ID에_가장_빨리_도착하는_첫번째_두번째_버스_조회(
            currentSchedule.getRegionName(), busStop.getNodeId(), busNames);
        // 버스 도착 정보만 배열로 따로 빼서 오름차순 정렬
        List<BusInfoDto> busInfoDtos = items.stream().map(
                i -> i.toBusInfoDto(i.getArrprevstationcnt(), i.getArrtime(), i.getNodeid(),
                    i.getNodenm(), i.getRouteid(), i.getRouteno(), i.getRoutetp(), i.getVehicletp()))
            .toList();
        // 요일과 시간대는 필드에 직접 주입
        return new ScheduleResponses(currentSchedule.getId(), currentSchedule.getName(),
            currentSchedule.getDaysList(), currentSchedule.getStartTime(),
            currentSchedule.getEndTime(), busStop.getName(), busInfoDtos,
            currentSchedule.getIsAlarmOn());
    }

    public List<ScheduleResponse> 오늘_스케줄들의_가장_빨리_도착하는_버스_정보(Long userId)
        throws UnsupportedEncodingException {
        User user = getUserById(userId);
        String today = DayConverter.getTodayAsString();
        List<Schedule> schedules = 유저의_특정_요일의_모든_스케줄_조회(user, today);

        List<ScheduleResponse> scheduleResponses = new ArrayList<>();
        for (Schedule s : schedules) {
            List<String> busNames = getBusNames(s.getBusStop());
            Item item = busService.특정_노드_ID에_가장_빨리_도착하는_버스_조회(s.getRegionName(),
                s.getBusStop().getNodeId(), busNames);
            scheduleResponses.add(
                item.toScheduleResponse(s.getId(), s.getName(), s.getDaysList(), s.getStartTime(),
                    s.getEndTime(), s.getIsAlarmOn()));
        }
        return scheduleResponses.stream().sorted(Comparator.comparing(ScheduleResponse::startTime))
            .toList();
    }

    public List<ScheduleResponses> 특정_요일의_스케줄들의_가장_빨리_도착하는_첫번째_두번째_버스_정보(Long userId, String days)
        throws UnsupportedEncodingException {
        User user = getUserById(userId);
        List<Schedule> schedules = 유저의_특정_요일의_모든_스케줄_조회(user, days);

        List<ScheduleResponses> scheduleResponsesList = new ArrayList<>();
        for (Schedule s : schedules) {
            List<String> busNames = getBusNames(s.getBusStop());
            List<Item> items = busService.특정_노드_ID에_가장_빨리_도착하는_첫번째_두번째_버스_조회(s.getRegionName(),
                s.getBusStop().getNodeId(), busNames);
            log.info("service items: {}", items);
            // 버스 도착 정보만 배열로 따로 빼서 오름차순 정렬
            List<BusInfoDto> busInfoDtos = items.stream().map(
                item -> item.toBusInfoDto(item.getArrprevstationcnt(), item.getArrtime(),
                    item.getNodeid(), item.getNodenm(), item.getRouteid(), item.getRouteno(),
                    item.getRoutetp(), item.getVehicletp())).toList();
            ScheduleResponses scheduleResponses = new ScheduleResponses(s.getId(), s.getName(),
                s.getDaysList(), s.getStartTime(), s.getEndTime(), s.getBusStop().getName(),
                busInfoDtos, s.getIsAlarmOn());
            scheduleResponsesList.add(scheduleResponses);
        }
        log.info("service: {}", scheduleResponsesList);
        return scheduleResponsesList.stream()
            .sorted(Comparator.comparing(responses -> responses.startTime())).toList();
    }

    private void 새_스케줄_생성시_겹침_유무_파악(User user, List<String> newDays, LocalTime newStartTime,
        LocalTime newEndTime) {
        List<Schedule> schedules = scheduleRepository.findAllByUser(user);

        for (Schedule s : schedules) {
            // 기존 스케줄의 요일 리스트
            List<String> existingDays = s.getDaysList();
            // 1. 요일이 겹치는지 확인
            boolean isDayOverlap = existingDays.stream().anyMatch(newDays::contains);
            if (!isDayOverlap) {
                continue; // 요일이 겹치지 않으면 스킵
            }
            // 2. 시간대가 겹치는지 확인
            boolean isTimeOverlap = (newStartTime.isBefore(s.getEndTime()) && newEndTime.isAfter(
                s.getStartTime()));
            // 둘 다 겹치면 충돌 발생
            if (isTimeOverlap) {
                throw new ScheduleException(ErrorCode.SCHEDULE_CONFLICT);
            }
        }
    }

    private void 기존_스케줄_수정시_겹침_유무_파악(User user, Long scheduleId, List<String> daysList,
        LocalTime startTime, LocalTime endTime) {
        List<Schedule> schedules = scheduleRepository.findAllByUser(user);
        for (Schedule existingSchedule : schedules) {
            // 1. 수정 중인 스케줄은 제외
            if (Objects.equals(scheduleId, existingSchedule.getId())) {
                continue;
            }
            // 2. 요일이 겹치는지 확인
            boolean isDayOverlap = existingSchedule.getDaysList().stream()
                .anyMatch(daysList::contains);
            // 3. 시간대가 겹치는지 확인
            boolean isTimeOverlap =
                startTime.isBefore(existingSchedule.getEndTime()) && endTime.isAfter(
                    existingSchedule.getStartTime());
            // 4. 둘 다 겹치면 예외 발생
            if (isDayOverlap && isTimeOverlap) {
                throw new ScheduleException(ErrorCode.SCHEDULE_CONFLICT);
            }
        }
    }

    private List<Schedule> 유저의_특정_요일의_모든_스케줄_조회(User user, String days) {
        return scheduleRepository.findAllByUserAndDays(user, days);
    }

    public Optional<Schedule> getCurrentSchedule(User user) {
        String today = DayConverter.getTodayAsString();
        return scheduleRepository.findByCurrentTimeAndDay(user, today, LocalTime.now());
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
    }

    private List<String> getBusNames(BusStop busStop) {
        return busStop.getBusList().stream().map(Bus::getName).toList();
    }

    @Transactional
    public boolean updateAlarm(Long userId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(
            () -> new ScheduleException(ErrorCode.SCHEDULE_NOT_FOUND));
        return schedule.updateAlarm();
    }
}