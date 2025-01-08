package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.bus.Bus;
import com.example.busnotice.domain.bus.BusRepository;
import com.example.busnotice.domain.bus.BusService;
import com.example.busnotice.domain.bus.res.BusStationArriveResponse.Item;
import com.example.busnotice.domain.busStop.BusStop;
import com.example.busnotice.domain.busStop.BusStopRepository;
import com.example.busnotice.domain.busStop.BusStopService;
import com.example.busnotice.domain.schedule.req.CreateScheduleRequest;
import com.example.busnotice.domain.schedule.res.ScheduleResponse;
import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.user.UserRepository;
import com.example.busnotice.global.code.StatusCode;
import com.example.busnotice.global.exception.schedule.ScheduleException;
import com.example.busnotice.global.jwt.JwtProvider;
import com.example.busnotice.util.DayConverter;
import java.io.UnsupportedEncodingException;
import java.time.LocalTime;
import java.util.ArrayList;
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
    public void createSchedule(String bearerToken, CreateScheduleRequest createScheduleRequest)
        throws UnsupportedEncodingException {
        User user = getUserByBearerToken(bearerToken);

        // 겹치는 스케줄 있는지 확인
        if (isScheduleOverLapping(user, createScheduleRequest.days(),
            createScheduleRequest.startTime(),
            createScheduleRequest.endTime())) {
            throw new ScheduleException(StatusCode.CONFLICT, "스케줄의 요일과 시간대가 겹칩니다.");
        }
        ;
        // 도시 코드 구하기
        String cityCode = busStopService.도시코드_조회(createScheduleRequest.regionName());
        // 스케줄상의 버스 정류장의 node id 구하기
        String nodeId = busService.버스정류장_노드_ID_조회(cityCode, createScheduleRequest.busStopName());
        // 스케줄상의 버스 정류장 생성
        BusStop busStop = BusStop.toEntity(cityCode, createScheduleRequest.busStopName(), nodeId);
        busStopRepository.save(busStop);
        // 해당 스케줄상의 버스 정류장에서 조회하고픈 버스 목록 등록
        List<String> busNames = createScheduleRequest.busList();
        List<Bus> busList = new ArrayList<>();
        for (String busName : busNames) {
            busList.add(Bus.toEntity(busStop, busName));
        }
        busRepository.saveAll(busList);
        // 스케줄 생성 후 생성한 버스 정류장 등록
        Schedule schedule = Schedule.toEntity(user, createScheduleRequest.name(),
            createScheduleRequest.days(),
            createScheduleRequest.startTime(), createScheduleRequest.endTime(), busStop);
        scheduleRepository.save(schedule);
    }

    private boolean isScheduleOverLapping(User user, String days, LocalTime startTime,
        LocalTime endTime) {
        List<Schedule> schedules = scheduleRepository.findAllByUser(user);
        for (Schedule existSchedule : schedules) {
            if (isDaysOverLapping(days, existSchedule.getDays())) {
                if (isTimeOverLapping(startTime, endTime, existSchedule.startTime,
                    existSchedule.endTime)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTimeOverLapping(LocalTime startTime, LocalTime endTime,
        LocalTime existStartTime, LocalTime existEndTime) {
        return (startTime.isBefore(existEndTime) && endTime.isAfter(existStartTime));
    }


    private boolean isDaysOverLapping(String newDays, String existDays) {
        if (newDays.equals(existDays)) {
            return true;
        }
        return false;
    }


    public Schedule getCurrentSchedule(User user) {
        String today = DayConverter.getTodayAsString();
        Schedule schedule = scheduleRepository.findByCurrentTimeAndDay(user,
            today, LocalTime.now());
        return schedule;
    }

    private User getUserByBearerToken(String bearerToken) {
        String token = jwtProvider.extractToken(bearerToken);
        String username = jwtProvider.getUsername(token);
        User user = userRepository.findByName(username);
        return user;
    }

    public ScheduleResponse 현재_스케줄의_가장_빨리_도착하는_버스_정보(String bearerToken) throws UnsupportedEncodingException {
        User user = getUserByBearerToken(bearerToken);
        // 현재 스케줄
        Schedule currentSchedule = getCurrentSchedule(user);
        // 현재 스케줄의 버스정류장
        BusStop busStop = currentSchedule.getBusStop();
        List<String> busNames = busStop.getBusList().stream().map(bus -> bus.getName()).toList();
        System.out.println(
            "현재 스케줄: " + currentSchedule.getName()
                + " " + busStop.getName()
                + " " + busNames);
        Item fastestBus = busService.특정_노드_ID에_가장_빨리_도착하는_버스_조회(busStop.getCityCode(),
            busStop.getNodeId(), busNames);
        return fastestBus.toResponseDto(currentSchedule.getDays(), currentSchedule.startTime, currentSchedule.endTime);
    }

    public List<ScheduleResponse> 오늘_스케줄들의_가장_빨리_도착하는_버스_정보(String bearerToken)
        throws UnsupportedEncodingException {
        User user = getUserByBearerToken(bearerToken);
        String today = DayConverter.getTodayAsString();
        List<Schedule> schedules = scheduleRepository.findAllByUserAndDays(user, today);
        List<ScheduleResponse> scheduleResponses = new ArrayList<>();
        for (Schedule schedule : schedules) {
            List<String> busNames = schedule.getBusStop().getBusList().stream()
                .map(bus -> bus.getName()).toList();
            Item item = busService.특정_노드_ID에_가장_빨리_도착하는_버스_조회(schedule.getBusStop().getCityCode(),
                schedule.getBusStop().getNodeId(), busNames);
            scheduleResponses.add(item.toResponseDto(schedule.getDays(), schedule.startTime, schedule.endTime));
        }
        return scheduleResponses;
    }
}
