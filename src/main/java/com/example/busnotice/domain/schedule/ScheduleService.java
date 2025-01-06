package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.bus.Bus;
import com.example.busnotice.domain.bus.BusRepository;
import com.example.busnotice.domain.bus.BusService;
import com.example.busnotice.domain.bus.res.BusStationResponse;
import com.example.busnotice.domain.busStop.BusStop;
import com.example.busnotice.domain.busStop.BusStopRepository;
import com.example.busnotice.domain.busStop.BusStopService;
import com.example.busnotice.domain.schedule.req.CreateScheduleRequest;
import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.user.UserRepository;
import com.example.busnotice.global.jwt.JwtProvider;
import java.io.UnsupportedEncodingException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Description;
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
    public Schedule createSchedule(String bearerToken, CreateScheduleRequest createScheduleRequest) throws UnsupportedEncodingException {
        User user = getUserByBearerToken(bearerToken);
        // 도시 코드 구하기
        String cityCode = busStopService.getCityCode(createScheduleRequest.regionName());
        // 스케줄상의 버스 정류장의 node id 구하기
        String nodeId = busService.getNodeId(cityCode, createScheduleRequest.busStopName());
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
        Schedule schedule = Schedule.toEntity(user, createScheduleRequest.name(), createScheduleRequest.startTime(), createScheduleRequest.endTime(), busStop);
        scheduleRepository.save(schedule);
        return schedule;
    }

    public List<Schedule> getAllSchedule(String bearerToken) {
        User user = getUserByBearerToken(bearerToken);

        List<Schedule> schedules = scheduleRepository.findAllByUser(user);
        return schedules;
    }

    public Schedule getCurrentSchedule(User user){
        Schedule schedule = scheduleRepository.findByCurrentTime(user,
            LocalTime.now());
        return schedule;
    }

//    @Description("현재 스케줄의 가장 빨리 도착하는 버스 정보 조회")
//    public String getCurrentBusInfo(String bearerToken) {
//        User user = getUserByBearerToken(bearerToken);
//        // 현재 스케줄
//        Schedule currentSchedule = getCurrentSchedule(user);
//        // 현재 스케줄의 버스정류장
//        BusStop busStop = currentSchedule.getBusStop();
//        busService.getMinArrTimeBus(busStop, busStop.getBusList());
//        return result.getResponse().getBody().getItems().getItem().getNodeid();
//        // 현재 스케줄의 버스정류장의 버스들
//        List<Bus> busList = currentSchedule.getBusStop().getBusList();
//        findMinArrTime(busList);
//
//    }

    private void findMinArrTime(List<Bus> busList) {

    }

    private User getUserByBearerToken(String bearerToken) {
        String token = jwtProvider.extractToken(bearerToken);
        String username = jwtProvider.getUsername(token);
        User user = userRepository.findByName(username);
        return user;
    }
}
