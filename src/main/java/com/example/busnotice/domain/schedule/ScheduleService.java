package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.bus.Bus;
import com.example.busnotice.domain.bus.BusRepository;
import com.example.busnotice.domain.busStop.BusStopSectionRepository;
import com.example.busnotice.domain.schedule.repository.ScheduleRepository;
import com.example.busnotice.domain.schedule.req.CreateScheduleRequest;
import com.example.busnotice.domain.schedule.req.UpdateScheduleRequest;
import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.user.UserRepository;
import com.example.busnotice.global.code.ErrorCode;
import com.example.busnotice.global.exception.ScheduleException;
import com.example.busnotice.global.exception.UserException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final BusStopSectionRepository busStopSectionRepository;
    private final SubwaySectionRepository subwaySectionRepository;
    private final BusRepository busRepository;
    private final SectionRepository sectionRepository;
    private final EntityManager entityManager;

    @Transactional
    public void createSchedule(Long userId, CreateScheduleRequest req) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 도착지 정보 생성
        CreateScheduleRequest.DestinationInfo crdi = req.destinationInfo();
        Schedule.DestinationInfo destinationInfo = new Schedule.DestinationInfo(
                crdi.type(),
                crdi.regionName(),
                crdi.placeName(),
                crdi.nodeId()
        );

        // 3. Schedule 엔티티 생성(Section은 나중에 추가)
        Schedule schedule = new Schedule(
                user,
                req.name(),
                req.daysList(),
                req.startTime(),
                req.endTime(),
                new ArrayList<>(), // sections
                destinationInfo,
                req.isAlarmOn()
        );
        Schedule savedSchedule = scheduleRepository.save(schedule);

        // 4. Section 생성 및 저장
        List<Section> sectionEntities = new ArrayList<>();
        List<CreateScheduleRequest.RouteInfo> routeInfos = req.routeInfos();
        for (int i = 0; i < routeInfos.size(); i++) {
            CreateScheduleRequest.RouteInfo route = routeInfos.get(i);

            if ("BUS".equalsIgnoreCase(route.type())) {
                // 4-1. BusStopSection 생성
                CreateScheduleRequest.BusStopSectionInfo bssi = route.busStopSection();
                BusStopSection busStopSection = new BusStopSection(
                        bssi.regionName(),
                        bssi.busStopName(),
                        bssi.nodeId(),
                        new ArrayList<>()
                );
                BusStopSection savedBusStopSection = busStopSectionRepository.save(busStopSection);

                // 4-2. Bus 생성 및 저장
                List<Bus> busEntities = new ArrayList<>();
                if (bssi.busList() != null) {
                    for (CreateScheduleRequest.BusInfo bi : bssi.busList()) {
                        Bus bus = new Bus(savedBusStopSection, bi.name(), bi.type());
                        busEntities.add(bus);
                    }
                    busRepository.saveAll(busEntities);
                    savedBusStopSection.setBusList(busEntities);
                }

                // 4-3. Section 생성 및 추가
                Section section = Section.busSection(i, savedSchedule, savedBusStopSection);
                sectionEntities.add(section);

            } else if ("SUBWAY".equalsIgnoreCase(route.type())) {
                // 4-4. SubwaySection 생성
                CreateScheduleRequest.SubwaySectionInfo ssi = route.subwaySection();
                SubwaySection subwaySection = new SubwaySection(
                        ssi.regionName(),
                        ssi.lineName(),
                        ssi.stationName(),
                        ssi.dir()
                );
                SubwaySection savedSubwaySection = subwaySectionRepository.save(subwaySection);

                // 4-5. Section 생성 및 추가
                Section section = Section.subwaySection(i, savedSchedule, savedSubwaySection);
                sectionEntities.add(section);

            } else {
                throw new IllegalArgumentException("지원하지 않는 구간 타입: " + route.type());
            }
        }

        // 5. Schedule에 Section 리스트 연결 및 저장
        savedSchedule.setSections(sectionEntities);
        scheduleRepository.save(savedSchedule);

        log.info("created schedule: {}", savedSchedule);
    }

    //
//    public ScheduleInfoResponse getSchedule(Long userId, Long scheduleId) {
//        Schedule schedule = scheduleRepository.findByIdAndUserId(scheduleId, userId)
//                .orElseThrow(() -> new ScheduleException(ErrorCode.SCHEDULE_NOT_FOUND));
//        return ScheduleInfoResponse.fromEntity(schedule);
//    }
//
    @Transactional
    public void updateSchedule(Long userId, Long scheduleId, UpdateScheduleRequest req) {
        // 1. 사용자 및 기존 스케줄 조회
        User user = getUserById(userId);
        Schedule existSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ErrorCode.SCHEDULE_NOT_FOUND));

        // 2. 겹치는 스케줄 유무 확인
        기존_스케줄_수정시_겹침_유무_파악(
                user, scheduleId, req.daysList(), req.startTime(), req.endTime()
        );

        // 3. 기존 Section, BusStopSection, SubwaySection, Bus 모두 삭제
        // (Section이 orphanRemoval = true, cascade = ALL로 설정되어 있어야 함)
        List<Section> sections = existSchedule.getSections();
        sections.clear(); // 기존 Section 모두 orphan 처리됨
        entityManager.flush();

        // 4. 새 Section, BusStopSection, SubwaySection, Bus 등록
        List<UpdateScheduleRequest.RouteInfo> routeInfos = req.routeInfos();
        for (int i = 0; i < routeInfos.size(); i++) {
            UpdateScheduleRequest.RouteInfo route = routeInfos.get(i);

            if ("BUS".equalsIgnoreCase(route.type())) {
                UpdateScheduleRequest.BusStopSectionInfo bssi = route.busStopSection();
                BusStopSection busStopSection = new BusStopSection(
                        bssi.regionName(),
                        bssi.busStopName(),
                        bssi.nodeId(),
                        new ArrayList<>()
                );
                BusStopSection savedBusStopSection = busStopSectionRepository.save(busStopSection);

                List<Bus> busEntities = new ArrayList<>();
                if (bssi.busList() != null) {
                    for (UpdateScheduleRequest.BusInfo bi : bssi.busList()) {
                        Bus bus = new Bus(savedBusStopSection, bi.name(), bi.type());
                        busEntities.add(bus);
                    }
                    busRepository.saveAll(busEntities);
                    savedBusStopSection.setBusList(busEntities);
                }

                Section section = Section.busSection(i, existSchedule, savedBusStopSection);
                sections.add(section);

            } else if ("SUBWAY".equalsIgnoreCase(route.type())) {
                UpdateScheduleRequest.SubwaySectionInfo ssi = route.subwaySection();
                SubwaySection subwaySection = new SubwaySection(
                        ssi.regionName(),
                        ssi.lineName(),
                        ssi.stationName(),
                        ssi.dir()
                );
                SubwaySection savedSubwaySection = subwaySectionRepository.save(subwaySection);

                Section section = Section.subwaySection(i, existSchedule, savedSubwaySection);
                sections.add(section);

            } else {
                throw new IllegalArgumentException("지원하지 않는 구간 타입: " + route.type());
            }
        }

        // 5. 도착지 정보 생성
        UpdateScheduleRequest.DestinationInfo crdi = req.destinationInfo();
        Schedule.DestinationInfo destinationInfo = new Schedule.DestinationInfo(
                crdi.type(),
                crdi.regionName(),
                crdi.placeName(),
                crdi.nodeId()
        );

        // 6. 스케줄 정보 업데이트 (sections 컬렉션은 이미 직접 조작)
        existSchedule.setName(req.name());
        existSchedule.setDaysList(req.daysList());
        existSchedule.setStartTime(req.startTime());
        existSchedule.setEndTime(req.endTime());
        existSchedule.setDestinationInfo(destinationInfo);
        existSchedule.setIsAlarmOn(req.isAlarmOn());

        scheduleRepository.save(existSchedule);
    }


    @Transactional
    public void deleteSchedule(Long userId, Long scheduleId) {
        User user = getUserById(userId);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ErrorCode.SCHEDULE_NOT_FOUND));
        if (!schedule.getUser().getId().equals(user.getId())) {
            throw new ScheduleException(ErrorCode.USER_UNAUTHORIZED);
        }
        scheduleRepository.delete(schedule); // 이미 조회한 엔티티로 삭제
    }

    //
//
//    public ScheduleResponse 현재_스케줄의_가장_빨리_도착하는_첫번째_두번째_버스_정보(Long userId)
//            throws UnsupportedEncodingException {
//        User user = getUserById(userId);
//        // 현재 스케줄
//        Optional<Schedule> optionalCurrentSchedule = getCurrentSchedule(user);
//        if (optionalCurrentSchedule.isEmpty()) {
//            return null;
//        }
//
//        Schedule cs = optionalCurrentSchedule.get();
//        List<BusStopArrInfoDto> busStopArrInfoDtos = new ArrayList<>();
//        List<BusArrInfoDto> busArrInfoDtos;
//
//        for (BusStop bs : cs.getBusStops()) {
//            List<String> busNames = bs.getBusList().stream().map(b -> b.getName()).toList();
//            List<Item> items = busService.특정_노드_ID에_가장_빨리_도착하는_첫번째_두번째_버스_조회(bs.getRegionName(),
//                    bs.getNodeId(), busNames);
//            // 버스 도착 정보 추출
//            busArrInfoDtos = items.stream().map(
//                    item -> item.toBusInfoDto(item.getArrprevstationcnt(), item.getArrtime(),
//                            item.getNodeid(), item.getNodenm(), item.getRouteid(), item.getRouteno(),
//                            item.getRoutetp(), item.getVehicletp())).toList();
//            // 버스정류장 정보 추출
//            busStopArrInfoDtos.add(new BusStopArrInfoDto(bs.getName(), busArrInfoDtos));
//        }
//        ScheduleResponse scheduleResponse = new ScheduleResponse(cs.getId(), cs.getName(),
//                cs.getDaysList(), cs.getStartTime(), cs.getEndTime(), busStopArrInfoDtos,
//                cs.getDestinationInfo().busStopName,
//                cs.getIsAlarmOn());
//        return scheduleResponse;
//    }
//
//    public List<ScheduleResponse> 특정_요일의_스케줄들의_가장_빨리_도착하는_첫번째_두번째_버스_정보(Long userId, String days)
//            throws UnsupportedEncodingException {
//        User user = getUserById(userId);
//        List<Schedule> schedules = 유저의_특정_요일의_모든_스케줄_조회(user, days);
//
//        List<ScheduleResponse> scheduleResponsesList = new ArrayList<>();
//        for (Schedule s : schedules) {
//            List<BusStop> busStops = s.getBusStops();
//            List<BusStopArrInfoDto> busStopArrInfoDtos = new ArrayList<>();
//            List<BusArrInfoDto> busArrInfoDtos;
//            for (BusStop bs : busStops) {
//                List<String> busNames = bs.getBusList().stream().map(b -> b.getName()).toList();
//                List<Item> items = busService.특정_노드_ID에_가장_빨리_도착하는_첫번째_두번째_버스_조회(bs.getRegionName(),
//                        bs.getNodeId(), busNames);
//                // 버스 도착 정보 추출
//                busArrInfoDtos = items.stream().map(
//                        item -> item.toBusInfoDto(item.getArrprevstationcnt(), item.getArrtime(),
//                                item.getNodeid(), item.getNodenm(), item.getRouteid(), item.getRouteno(),
//                                item.getRoutetp(), item.getVehicletp())).toList();
//                // 버스정류장 정보 추출
//                busStopArrInfoDtos.add(new BusStopArrInfoDto(bs.getName(), busArrInfoDtos));
//            }
//            ScheduleResponse scheduleResponse = new ScheduleResponse(s.getId(), s.getName(),
//                    s.getDaysList(), s.getStartTime(), s.getEndTime(), busStopArrInfoDtos,
//                    s.getDestinationInfo().busStopName,
//                    s.getIsAlarmOn());
//            scheduleResponsesList.add(scheduleResponse);
//        }
//        return scheduleResponsesList.stream()
//                .sorted(Comparator.comparing(responses -> responses.startTime())).toList();
//    }
//
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

    //
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

    //
//    private List<Schedule> 유저의_특정_요일의_모든_스케줄_조회(User user, String days) {
//        return scheduleRepository.findAllByUserAndDays(user, days);
//    }
//
//    public Optional<Schedule> getCurrentSchedule(User user) {
//        String today = DayConverter.getTodayAsString();
//        return scheduleRepository.findByCurrentTimeAndDay(user, today, LocalTime.now());
//    }
//
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
    }
//
//    private List<String> getBusNames(BusStop busStop) {
//        return busStop.getBusList().stream().map(Bus::getName).toList();
//    }
//
//    @Transactional
//    public boolean updateAlarm(Long userId, Long scheduleId) {
//        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(
//                () -> new ScheduleException(ErrorCode.SCHEDULE_NOT_FOUND));
//        if (schedule.getUser().getId() != userId) {
//            throw new ScheduleException(ErrorCode.USER_UNAUTHORIZED);
//        }
//        return schedule.updateAlarm();
//    }
}