package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.bus.Bus;
import com.example.busnotice.domain.bus.BusRepository;
import com.example.busnotice.domain.busStop.BusStop;
import com.example.busnotice.domain.busStop.BusStopRepository;
import com.example.busnotice.domain.schedule.req.CreateScheduleRequest;
import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.user.UserRepository;
import com.example.busnotice.global.jwt.JwtProvider;
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

    @Transactional
    public Schedule createSchedule(String bearerToken, CreateScheduleRequest createScheduleRequest) {
        String token = jwtProvider.extractToken(bearerToken);
        String username = jwtProvider.getUsername(token);
        User user = userRepository.findByName(username);

        // 스케줄상의 버스 정류장 생성
        BusStop busStop = BusStop.toEntity(createScheduleRequest.name());
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
}
