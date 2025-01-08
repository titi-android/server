package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.bus.Bus;
import com.example.busnotice.domain.bus.BusRepository;
import com.example.busnotice.domain.bus.BusService;
import com.example.busnotice.domain.bus.res.BusStationArriveResponse.Item;
import com.example.busnotice.domain.busStop.BusStop;
import com.example.busnotice.domain.busStop.BusStopRepository;
import com.example.busnotice.domain.busStop.BusStopService;
import com.example.busnotice.domain.schedule.req.CreateScheduleRequest;
import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.user.UserRepository;
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
    public Schedule createSchedule(String bearerToken, CreateScheduleRequest createScheduleRequest)
        throws UnsupportedEncodingException {
        User user = getUserByBearerToken(bearerToken);
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
        Schedule schedule = Schedule.toEntity(user, createScheduleRequest.name(),createScheduleRequest.days(),
            createScheduleRequest.startTime(), createScheduleRequest.endTime(), busStop);
        scheduleRepository.save(schedule);
        return schedule;
    }

    public List<Schedule> getAllSchedule(String bearerToken) {
        User user = getUserByBearerToken(bearerToken);

        List<Schedule> schedules = scheduleRepository.findAllByUser(user);
        return schedules;
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

    public Item 현재_스케줄의_가장_빨리_도착하는_버스_정보(String bearerToken) throws UnsupportedEncodingException {
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
        return fastestBus;
    }
}
