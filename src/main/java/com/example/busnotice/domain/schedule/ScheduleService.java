package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.bus.Bus;
import com.example.busnotice.domain.bus.BusRepository;
import com.example.busnotice.domain.bus.BusService;
import com.example.busnotice.domain.bus.res.BusArrInfosDto;
import com.example.busnotice.domain.busStop.BusStopSection;
import com.example.busnotice.domain.busStop.BusStopSectionRepository;
import com.example.busnotice.domain.schedule.repository.ScheduleRepository;
import com.example.busnotice.domain.schedule.req.CreateScheduleRequest;
import com.example.busnotice.domain.schedule.req.UpdateScheduleRequest;
import com.example.busnotice.domain.schedule.res.ScheduleArrivalResponse;
import com.example.busnotice.domain.schedule.res.ScheduleInfoResponse;
import com.example.busnotice.domain.section.Section;
import com.example.busnotice.domain.subway.*;
import com.example.busnotice.domain.subway.dto.RealtimeArrResponse;
import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.user.UserRepository;
import com.example.busnotice.global.code.ErrorCode;
import com.example.busnotice.global.exception.ScheduleException;
import com.example.busnotice.global.exception.UserException;
import com.example.busnotice.util.DayConverter;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final BusStopSectionRepository busStopSectionRepository;
    private final SubwaySectionRepository subwaySectionRepository;
    private final BusRepository busRepository;
    private final EntityManager entityManager;
    private final BusService busService;
    private final SubwaySectionService subwaySectionService;

    @Transactional
    @CacheEvict(value = {"scheduleInfo", "schedulesByDay"}, allEntries = true)
    public void createSchedule(Long userId, CreateScheduleRequest req) {

        새_스케줄_생성시_겹침_유무_파악(getUserById(userId), req.daysList(), req.startTime(), req.endTime());

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 도착지 정보 생성
        CreateScheduleRequest.DestinationInfo crdi = req.destinationInfo();
        Schedule.DestinationInfo destinationInfo = new Schedule.DestinationInfo();
        if ("BUS".equalsIgnoreCase(crdi.type())) { // 버스인 경우
            destinationInfo = new Schedule.DestinationInfo(
                    crdi.type(),
                    crdi.regionName(),
                    crdi.desName()
            );
        } else if ("SUBWAY".equalsIgnoreCase(crdi.type())) { // 지하철인 경우
            destinationInfo = new Schedule.DestinationInfo(
                    crdi.type(),
                    crdi.regionName(),
                    crdi.desName(),
                    crdi.lineName(),
                    crdi.dirName(),
                    crdi.dir()
            );
        }

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
                        ssi.dirName(),
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

    @Cacheable(value = "scheduleInfo", key = "#userId + '_' + #scheduleId")
    public ScheduleInfoResponse getSchedule(Long userId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ErrorCode.SCHEDULE_NOT_FOUND));
        if (!schedule.getUser().getId().equals(userId)) {
            throw new ScheduleException(ErrorCode.USER_UNAUTHORIZED);
        }

        // Section 리스트를 orderIndex 순서대로 변환
        List<ScheduleInfoResponse.RouteInfo> routeInfos = schedule.getSections().stream()
                .sorted(Comparator.comparingInt(Section::getOrderIndex))
                .map(section -> {
                    if ("BUS".equalsIgnoreCase(section.getType())) {
                        BusStopSection bss = section.getBusStopSection();
                        List<ScheduleInfoResponse.BusInfo> busList = bss.getBusList().stream()
                                .map(bus -> new ScheduleInfoResponse.BusInfo(bus.getName(), bus.getType()))
                                .toList();
                        return new ScheduleInfoResponse.RouteInfo(
                                "BUS",
                                new ScheduleInfoResponse.BusStopSectionInfo(
                                        bss.getRegionName(),
                                        bss.getBusStopName(),
                                        bss.getNodeId(),
                                        busList
                                ),
                                null
                        );
                    } else if ("SUBWAY".equalsIgnoreCase(section.getType())) {
                        SubwaySection ss = section.getSubwaySection();
                        return new ScheduleInfoResponse.RouteInfo(
                                "SUBWAY",
                                null,
                                new ScheduleInfoResponse.SubwaySectionInfo(
                                        ss.getRegionName(),
                                        ss.getLineName(),
                                        ss.getStationName(),
                                        ss.getDirName(),
                                        ss.getDir()
                                )
                        );
                    } else {
                        throw new IllegalStateException("알 수 없는 구간 타입: " + section.getType());
                    }
                })
                .toList();

        Schedule.DestinationInfo di = schedule.getDestinationInfo();
        ScheduleInfoResponse.DestinationInfo destinationInfo = new ScheduleInfoResponse.DestinationInfo(
                di.getType(), di.getDesName()
        );

        return new ScheduleInfoResponse(
                schedule.getId(),
                schedule.getName(),
                schedule.getDaysList(),
                schedule.getStartTime().toString(),
                schedule.getEndTime().toString(),
                schedule.getIsAlarmOn(),
                destinationInfo,
                routeInfos
        );
    }


    @Transactional
    @CacheEvict(value = {"scheduleInfo", "schedulesByDay"}, allEntries = true)
    public void updateSchedule(Long userId, Long scheduleId, UpdateScheduleRequest req) {

        기존_스케줄_수정시_겹침_유무_파악(getUserById(userId), scheduleId, req.daysList(), req.startTime(), req.endTime());

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
                        ssi.dirName(),
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
        Schedule.DestinationInfo destinationInfo = new Schedule.DestinationInfo();
        if ("BUS".equalsIgnoreCase(crdi.type())) { // 버스인 경우
            destinationInfo = new Schedule.DestinationInfo(
                    crdi.type(),
                    crdi.regionName(),
                    crdi.desName()
            );
        } else if ("SUBWAY".equalsIgnoreCase(crdi.type())) { // 지하철인 경우
            destinationInfo = new Schedule.DestinationInfo(
                    crdi.type(),
                    crdi.regionName(),
                    crdi.desName(),
                    crdi.lineName(),
                    crdi.dirName(),
                    crdi.dir()
            );
        }

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
    @CacheEvict(value = {"scheduleInfo", "schedulesByDay"}, allEntries = true)
    public void deleteSchedule(Long userId, Long scheduleId) {
        User user = getUserById(userId);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ErrorCode.SCHEDULE_NOT_FOUND));
        if (!schedule.getUser().getId().equals(user.getId())) {
            throw new ScheduleException(ErrorCode.USER_UNAUTHORIZED);
        }
        scheduleRepository.delete(schedule); // 이미 조회한 엔티티로 삭제
    }

    // (v3) 특정 요일의 모든 스케줄의 도착 정보 조회
    @Transactional(readOnly = true)
    public List<ScheduleArrivalResponse> 특정_요일의_스케줄들의_교통편_정보(Long userId, String day) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 해당 사용자의 특정 요일 스케줄 조회
        List<Schedule> schedules = 유저의_특정_요일의_모든_스케줄_조회(user, day);

        List<ScheduleArrivalResponse> responseList = new ArrayList<>();

        for (Schedule schedule : schedules) {
            List<ScheduleArrivalResponse.SectionArrInfoDto> sectionDtos = new ArrayList<>();

            for (Section section : schedule.getSections()) {
                if ("BUS".equalsIgnoreCase(section.getType())) {
                    BusStopSection bss = section.getBusStopSection();
                    List<String> busNames = bss.getBusList().stream()
                            .map(Bus::getName)
                            .collect(Collectors.toList());

                    // 버스 도착 정보 조회 (가장 빠른 2개)
                    List<BusArrInfosDto.Item> items = busService.특정_노드_ID에_가장_빨리_도착하는_첫번째_두번째_버스_조회(
                            bss.getRegionName(), bss.getNodeId(), busNames
                    );

                    List<ScheduleArrivalResponse.SectionArrInfoDto.BusStopArrInfo.BusArrInfo> busArrivals = items.stream()
                            .map(item -> new ScheduleArrivalResponse.SectionArrInfoDto.BusStopArrInfo.BusArrInfo(
                                    item.getArrprevstationcnt(),
                                    item.getArrtime(),
                                    item.getNodeid(),
                                    item.getNodenm(),
                                    item.getRouteid(),
                                    item.getRouteno(),
                                    item.getRoutetp(),
                                    item.getVehicletp()
                            ))
                            .collect(Collectors.toList());

                    sectionDtos.add(new ScheduleArrivalResponse.SectionArrInfoDto(
                            "BUS",
                            new ScheduleArrivalResponse.SectionArrInfoDto.BusStopArrInfo(
                                    bss.getBusStopName(),
                                    busArrivals
                            ),
                            null,
                            section.getOrderIndex()
                    ));
                } else if ("SUBWAY".equalsIgnoreCase(section.getType())) {
                    SubwaySection ss = section.getSubwaySection();

                    // Enum 변환 (LineType, LineDir)
                    LineType lineType = LineType.fromDisplayName(ss.getLineName());
                    LineDir dir = LineDir.fromDisplayName(ss.getDir());

                    List<RealtimeArrResponse.RealtimeArrival> arrivals =
                            subwaySectionService.getStationArrInfo(lineType, ss.getStationName(), dir);

                    // 가장 빠른 2개만 추출
                    List<RealtimeArrResponse.RealtimeArrival> top2 = arrivals.stream()
                            .sorted(Comparator.comparingInt(a -> {
                                try {
                                    return Integer.parseInt(a.getBarvlDt());
                                } catch (Exception e) {
                                    return Integer.MAX_VALUE;
                                }
                            }))
                            .limit(2)
                            .collect(Collectors.toList());

                    List<ScheduleArrivalResponse.SectionArrInfoDto.SubwayArrInfo.SubwayArrival> subwayArrivals = top2.stream()
                            .map(a -> new ScheduleArrivalResponse.SectionArrInfoDto.SubwayArrInfo.SubwayArrival(
                                    a.getSubwayId(),
                                    a.getUpdnLine(),
                                    a.getStatnNm(),
                                    a.getBarvlDt(),
                                    a.getArvlMsg2(),
                                    a.getArvlMsg3(),
                                    a.getArvlCd()
                            ))
                            .collect(Collectors.toList());

                    sectionDtos.add(new ScheduleArrivalResponse.SectionArrInfoDto(
                            "SUBWAY",
                            null,
                            new ScheduleArrivalResponse.SectionArrInfoDto.SubwayArrInfo(
                                    ss.getRegionName(),
                                    ss.getLineName(),
                                    ss.getStationName(),
                                    ss.getDirName(),
                                    ss.getDir(),
                                    subwayArrivals
                            ),
                            section.getOrderIndex()
                    ));
                }
            }

            // 환승 순서대로 정렬
            sectionDtos.sort(Comparator.comparingInt(ScheduleArrivalResponse.SectionArrInfoDto::orderIndex));

            responseList.add(new ScheduleArrivalResponse(
                    schedule.getId(),
                    schedule.getName(),
                    schedule.getDaysList(),
                    schedule.getStartTime(),
                    schedule.getEndTime(),
                    sectionDtos,
                    schedule.getDestinationInfo().getType(),
                    schedule.getDestinationInfo().getDesName(),
                    schedule.getIsAlarmOn()
            ));
        }

        // 스케줄 시작 시간 기준 정렬
        return responseList.stream()
                .sorted(Comparator.comparing(ScheduleArrivalResponse::startTime))
                .collect(Collectors.toList());
    }

    public ScheduleArrivalResponse 현재_스케줄의_교통편_정보(Long userId) throws UnsupportedEncodingException {
        // 1. 사용자 조회
        User user = getUserById(userId);

        // 2. 현재 스케줄 조회 (예: 오늘 날짜, 현재 시간 기준)
        Optional<Schedule> optionalCurrentSchedule = getCurrentSchedule(user);
        if (optionalCurrentSchedule.isEmpty()) {
            return null;
        }

        Schedule cs = optionalCurrentSchedule.get();
        List<ScheduleArrivalResponse.SectionArrInfoDto> sectionDtos = new ArrayList<>();

        // 3. Section 기반 환승 구간 순회 (순서 보장)
        for (Section section : cs.getSections()) {
            if ("BUS".equalsIgnoreCase(section.getType())) {
                BusStopSection bss = section.getBusStopSection();
                List<String> busNames = bss.getBusList().stream()
                        .map(Bus::getName)
                        .toList();

                // 버스 도착 정보 조회 (가장 빠른 2개)
                List<BusArrInfosDto.Item> items = busService.특정_노드_ID에_가장_빨리_도착하는_첫번째_두번째_버스_조회(
                        bss.getRegionName(), bss.getNodeId(), busNames
                );

                List<ScheduleArrivalResponse.SectionArrInfoDto.BusStopArrInfo.BusArrInfo> busArrivals = items.stream()
                        .map(item -> new ScheduleArrivalResponse.SectionArrInfoDto.BusStopArrInfo.BusArrInfo(
                                item.getArrprevstationcnt(),
                                item.getArrtime(),
                                item.getNodeid(),
                                item.getNodenm(),
                                item.getRouteid(),
                                item.getRouteno(),
                                item.getRoutetp(),
                                item.getVehicletp()
                        ))
                        .toList();

                sectionDtos.add(new ScheduleArrivalResponse.SectionArrInfoDto(
                        "BUS",
                        new ScheduleArrivalResponse.SectionArrInfoDto.BusStopArrInfo(
                                bss.getBusStopName(),
                                busArrivals
                        ),
                        null,
                        section.getOrderIndex()
                ));
            } else if ("SUBWAY".equalsIgnoreCase(section.getType())) {
                SubwaySection ss = section.getSubwaySection();

                // Enum 변환 (LineType, LineDir)
                LineType lineType = LineType.fromDisplayName(ss.getLineName());
                LineDir dir = LineDir.fromDisplayName(ss.getDir());

                List<RealtimeArrResponse.RealtimeArrival> arrivals =
                        subwaySectionService.getStationArrInfo(lineType, ss.getStationName(), dir);

                // 가장 빠른 2개만 추출
                List<RealtimeArrResponse.RealtimeArrival> top2 = arrivals.stream()
                        .sorted(Comparator.comparingInt(a -> {
                            try {
                                return Integer.parseInt(a.getBarvlDt());
                            } catch (Exception e) {
                                return Integer.MAX_VALUE;
                            }
                        }))
                        .limit(2)
                        .toList();

                List<ScheduleArrivalResponse.SectionArrInfoDto.SubwayArrInfo.SubwayArrival> subwayArrivals = top2.stream()
                        .map(a -> new ScheduleArrivalResponse.SectionArrInfoDto.SubwayArrInfo.SubwayArrival(
                                a.getSubwayId(),
                                a.getUpdnLine(),
                                a.getStatnNm(),
                                a.getBarvlDt(),
                                a.getArvlMsg2(),
                                a.getArvlMsg3(),
                                a.getArvlCd()
                        ))
                        .toList();

                sectionDtos.add(new ScheduleArrivalResponse.SectionArrInfoDto(
                        "SUBWAY",
                        null,
                        new ScheduleArrivalResponse.SectionArrInfoDto.SubwayArrInfo(
                                ss.getRegionName(),
                                ss.getLineName(),
                                ss.getStationName(),
                                ss.getDirName(),
                                ss.getDir(),
                                subwayArrivals
                        ),
                        section.getOrderIndex()
                ));
            }
        }

        // 환승 순서대로 정렬
        sectionDtos.sort(Comparator.comparingInt(ScheduleArrivalResponse.SectionArrInfoDto::orderIndex));

        // 4. 최종 응답 생성
        return new ScheduleArrivalResponse(
                cs.getId(),
                cs.getName(),
                cs.getDaysList(),
                cs.getStartTime(),
                cs.getEndTime(),
                sectionDtos,
                cs.getDestinationInfo().getType(),
                cs.getDestinationInfo().getDesName(),
                cs.getIsAlarmOn()
        );
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

    private List<Schedule> 유저의_특정_요일의_모든_스케줄_조회(User user, String days) {
        log.info("(시작) 유저의 특정 요일의 모든 스케줄 조회");
        List<Schedule> allByUserAndDays = scheduleRepository.findAllByUserAndDaysOrderByStartTime(user, days);
        log.info("(종료) 유저의 특정 요일의 모든 스케줄 조회");
        return allByUserAndDays;
    }

    public Optional<Schedule> getCurrentSchedule(User user) {
        String today = DayConverter.getTodayAsString();
        return scheduleRepository.findByCurrentTimeAndDay(user, today, LocalTime.now());
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
    }

    private List<String> getBusNames(BusStopSection busStop) {
        return busStop.getBusList().stream().map(Bus::getName).toList();
    }

    @Transactional
    @CacheEvict(value = {"scheduleInfo", "schedulesByDay"}, allEntries = true)
    public boolean updateAlarm(Long userId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new ScheduleException(ErrorCode.SCHEDULE_NOT_FOUND));
        if (schedule.getUser().getId() != userId) {
            throw new ScheduleException(ErrorCode.USER_UNAUTHORIZED);
        }
        return schedule.updateAlarmStatus();
    }

    @Transactional
    @CacheEvict(value = {"scheduleInfo", "schedulesByDay"}, allEntries = true)
    public void deleteSchedules(Long userId, List<Long> scheduleIds) {
        User user = getUserById(userId);

        List<Schedule> schedules = scheduleRepository.findAllById(scheduleIds);
        // 없는 ID 검증
        if (schedules.size() != scheduleIds.size()) {
            throw new ScheduleException(ErrorCode.SCHEDULE_NOT_FOUND);
        }
        // 소유자 검증
        boolean unauthorized = schedules.stream()
                .anyMatch(schedule -> !schedule.getUser().getId().equals(user.getId()));
        if (unauthorized) {
            throw new ScheduleException(ErrorCode.USER_UNAUTHORIZED);
        }
        // 일괄 삭제
        scheduleRepository.deleteAll(schedules);
    }
}