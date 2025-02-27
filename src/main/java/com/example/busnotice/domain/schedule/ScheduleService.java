package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.bus.Bus;
import com.example.busnotice.domain.bus.BusRepository;
import com.example.busnotice.domain.bus.BusService;
import com.example.busnotice.domain.bus.res.BusArrInfosDto.Item;
import com.example.busnotice.domain.busStop.BusStop;
import com.example.busnotice.domain.busStop.BusStopRepository;
import com.example.busnotice.domain.busStop.BusStopService;
import com.example.busnotice.domain.schedule.Schedule.DestinationInfo;
import com.example.busnotice.domain.schedule.repository.ScheduleRepository;
import com.example.busnotice.domain.schedule.req.CreateScheduleRequest;
import com.example.busnotice.domain.schedule.req.UpdateScheduleRequest;
import com.example.busnotice.domain.schedule.res.ScheduleInfoResponse;
import com.example.busnotice.domain.schedule.res.ScheduleResponse;
import com.example.busnotice.domain.schedule.res.ScheduleResponse.BusStopArrInfoDto;
import com.example.busnotice.domain.schedule.res.ScheduleResponse.BusStopArrInfoDto.BusArrInfoDto;
import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.user.UserRepository;
import com.example.busnotice.global.code.ErrorCode;
import com.example.busnotice.global.exception.ScheduleException;
import com.example.busnotice.global.exception.UserException;
import com.example.busnotice.util.DayConverter;
import jakarta.persistence.EntityManager;
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
    private final EntityManager entityManager;

    @Transactional
    public void createSchedule(Long userId, CreateScheduleRequest createScheduleRequest) {
        User user = getUserById(userId);

        // 겹치는 스케줄 있는지 확인
        새_스케줄_생성시_겹침_유무_파악(user, createScheduleRequest.daysList(),
            createScheduleRequest.startTime(), createScheduleRequest.endTime());

        // 스케줄 생성한 후 생성한 도착지 및 버스 정류장 등록
        CreateScheduleRequest.DestinationInfo crdi = createScheduleRequest.destinationInfo();
        DestinationInfo destinationInfo = new DestinationInfo(crdi.regionName(), crdi.busStopName(),
            crdi.nodeId());

        Schedule schedule = Schedule.toEntity(user, createScheduleRequest.name(),
            createScheduleRequest.daysList(), createScheduleRequest.startTime(),
            createScheduleRequest.endTime(), new ArrayList<>(),
            destinationInfo, createScheduleRequest.isAlarmOn()); // BusStop 은 나중에 추가

        Schedule savedSchedule = scheduleRepository.save(schedule);

        // BusStop 생성 및 schedule 설정
        List<CreateScheduleRequest.RouteInfo> routeInfos = createScheduleRequest.routeInfos();
        List<BusStop> busStops = new ArrayList<>();

        for (CreateScheduleRequest.RouteInfo ri : routeInfos) {
            String cityCode = busStopService.도시코드_DB_조회(ri.regionName());
            // 버스정류장 생성
            BusStop savedBusStop = busStopRepository.save(
                BusStop.toEntity(savedSchedule, cityCode, ri.regionName(), ri.busStopName(),
                    ri.nodeId()));
            busStops.add(savedBusStop);
            // 해당 버스정류장에 등록될 버스들 생성
            List<Bus> buses = ri.busInfos().stream()
                .map(bi -> Bus.toEntity(savedBusStop, bi.name(), bi.type()))
                .toList();
            busRepository.saveAll(buses);
        }
        savedSchedule.setBusStops(busStops);
        log.info("created schedule: {}", savedSchedule);
    }

    public ScheduleInfoResponse getSchedule(Long userId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findByIdAndUserId(scheduleId, userId)
            .orElseThrow(() -> new ScheduleException(ErrorCode.SCHEDULE_NOT_FOUND));
        return ScheduleInfoResponse.fromEntity(schedule);
    }

    @Transactional
    public void updateSchedule(Long userId, Long scheduleId,
        UpdateScheduleRequest updateScheduleRequest) {
        User user = getUserById(userId);
        Schedule existSchedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new ScheduleException(ErrorCode.SCHEDULE_NOT_FOUND));

        // 수정 전 스케줄을 제외하고, 수정한 스케줄과 겹치는 스케줄 있는지 확인
        기존_스케줄_수정시_겹침_유무_파악(user, scheduleId, updateScheduleRequest.daysList(),
            updateScheduleRequest.startTime(), updateScheduleRequest.endTime());

        // 수정 대상 스케줄의 BusStops 및 BusStop 에 등록된 Bus 들 모두 삭제
        busStopRepository.deleteAll(
            existSchedule.getBusStops());// busStop 과 bus 모두 삭제됨 (둘 다 Cascade ALL, orphanremoval true 로 설정했기 때문)
        entityManager.flush();

        // 새로 BusStop 및 Bus 등록
        List<UpdateScheduleRequest.RouteInfo> routeInfos = updateScheduleRequest.routeInfos();
        List<BusStop> busStops = new ArrayList<>();
        for (UpdateScheduleRequest.RouteInfo ri : routeInfos) {
            // 도시 코드
            String cityCode = busStopService.도시코드_DB_조회(ri.regionName());
            // 버스정류장 생성
            BusStop savedBusStop = busStopRepository.save(
                BusStop.toEntity(existSchedule, cityCode, ri.regionName(), ri.busStopName(),
                    ri.nodeId()));
            busStops.add(savedBusStop);
            // 해당 버스정류장에 등록될 버스들 생성
            List<Bus> buses = ri.busInfos().stream()
                .map(bi -> Bus.toEntity(savedBusStop, bi.name(), bi.type()))
                .toList();
            busRepository.saveAll(buses);
        }
        // 스케줄 생성한 후 생성한 도착지 및 버스 정류장 등록
        UpdateScheduleRequest.DestinationInfo crdi = updateScheduleRequest.destinationInfo();
        DestinationInfo destinationInfo = new DestinationInfo(crdi.regionName(), crdi.busStopName(),
            crdi.nodeId());
        // 스케줄 업데이트
        existSchedule.update(
            updateScheduleRequest.name(),
            updateScheduleRequest.daysList(),
            updateScheduleRequest.startTime(), updateScheduleRequest.endTime(),
            busStops, destinationInfo, updateScheduleRequest.isAlarmOn()
        );
    }

    @Transactional
    public void deleteSchedule(Long userId, Long scheduleId) {
        User user = getUserById(userId);
        Schedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new ScheduleException(ErrorCode.SCHEDULE_NOT_FOUND));
        if (schedule.getUser().getId() != user.getId()) {
            throw new ScheduleException(ErrorCode.USER_UNAUTHORIZED);
        }
        scheduleRepository.deleteById(scheduleId);
    }


    public ScheduleResponse 현재_스케줄의_가장_빨리_도착하는_첫번째_두번째_버스_정보(Long userId)
        throws UnsupportedEncodingException {
        User user = getUserById(userId);
        // 현재 스케줄
        Optional<Schedule> optionalCurrentSchedule = getCurrentSchedule(user);
        if (optionalCurrentSchedule.isEmpty()) {
            return null;
        }

        Schedule cs = optionalCurrentSchedule.get();
        List<BusStopArrInfoDto> busStopArrInfoDtos = new ArrayList<>();
        List<BusArrInfoDto> busArrInfoDtos;

        for (BusStop bs : cs.getBusStops()) {
            List<String> busNames = bs.getBusList().stream().map(b -> b.getName()).toList();
            List<Item> items = busService.특정_노드_ID에_가장_빨리_도착하는_첫번째_두번째_버스_조회(bs.getRegionName(),
                bs.getNodeId(), busNames);
            // 버스 도착 정보 추출
            busArrInfoDtos = items.stream().map(
                item -> item.toBusInfoDto(item.getArrprevstationcnt(), item.getArrtime(),
                    item.getNodeid(), item.getNodenm(), item.getRouteid(), item.getRouteno(),
                    item.getRoutetp(), item.getVehicletp())).toList();
            // 버스정류장 정보 추출
            busStopArrInfoDtos.add(new BusStopArrInfoDto(bs.getName(), busArrInfoDtos));
        }
        ScheduleResponse scheduleResponse = new ScheduleResponse(cs.getId(), cs.getName(),
            cs.getDaysList(), cs.getStartTime(), cs.getEndTime(), busStopArrInfoDtos,
            cs.getIsAlarmOn());
        return scheduleResponse;
    }

    public List<ScheduleResponse> 특정_요일의_스케줄들의_가장_빨리_도착하는_첫번째_두번째_버스_정보(Long userId, String days)
        throws UnsupportedEncodingException {
        User user = getUserById(userId);
        List<Schedule> schedules = 유저의_특정_요일의_모든_스케줄_조회(user, days);

        List<ScheduleResponse> scheduleResponsesList = new ArrayList<>();
        for (Schedule s : schedules) {
            List<BusStop> busStops = s.getBusStops();
            List<BusStopArrInfoDto> busStopArrInfoDtos = new ArrayList<>();
            List<BusArrInfoDto> busArrInfoDtos;
            for (BusStop bs : busStops) {
                List<String> busNames = bs.getBusList().stream().map(b -> b.getName()).toList();
                List<Item> items = busService.특정_노드_ID에_가장_빨리_도착하는_첫번째_두번째_버스_조회(bs.getRegionName(),
                    bs.getNodeId(), busNames);
                // 버스 도착 정보 추출
                busArrInfoDtos = items.stream().map(
                    item -> item.toBusInfoDto(item.getArrprevstationcnt(), item.getArrtime(),
                        item.getNodeid(), item.getNodenm(), item.getRouteid(), item.getRouteno(),
                        item.getRoutetp(), item.getVehicletp())).toList();
                // 버스정류장 정보 추출
                busStopArrInfoDtos.add(new BusStopArrInfoDto(bs.getName(), busArrInfoDtos));
            }
            ScheduleResponse scheduleResponse = new ScheduleResponse(s.getId(), s.getName(),
                s.getDaysList(), s.getStartTime(), s.getEndTime(), busStopArrInfoDtos,
                s.getIsAlarmOn());
            scheduleResponsesList.add(scheduleResponse);
        }
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
        if (schedule.getUser().getId() != userId) {
            throw new ScheduleException(ErrorCode.USER_UNAUTHORIZED);
        }
        return schedule.updateAlarm();
    }
}