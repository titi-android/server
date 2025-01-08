package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.bus.res.BusStationArriveResponse.Item;
import com.example.busnotice.domain.schedule.req.CreateScheduleRequest;
import java.io.UnsupportedEncodingException;
import java.util.List;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/schedule")
    @Description("스케줄 등록")
    public ResponseEntity<String> createSchedule(
        @RequestBody CreateScheduleRequest createScheduleRequest,
        @RequestHeader("Authorization") String bearerToken
    ) throws UnsupportedEncodingException {
        Schedule schedule = scheduleService.createSchedule(bearerToken, createScheduleRequest);
        return ResponseEntity.status(HttpStatus.OK).body(schedule.toString());
    }

    @GetMapping("/schedule/all")
    @Description("모든 스케줄 조회 - 모든 스케줄의 가장 빠른 버스 정보 조회")
    public List<Schedule> getAllSchedules(
        @RequestHeader("Authorization") String bearerToken
    ) {
        List<Schedule> schedules = scheduleService.getAllSchedule(bearerToken);
        return schedules;
    }

    @GetMapping("/schedule/now")
    @Description("현재 시각의 스케줄 조회 - 현재 스케줄의 가장 빠른 버스 정보 조회")
    public Item getCurrentSchedule(
        @RequestHeader("Authorization") String bearerToken
    ) throws UnsupportedEncodingException {
        Item fastestBus = scheduleService.현재_스케줄의_가장_빨리_도착하는_버스_정보(bearerToken);
        return fastestBus;
    }

    @GetMapping("/schedule/{scheduleId}")
    @Description("특정 스케줄 상세 조회 - 특정 스케줄의 상세 정보 조회")
    public List<Schedule> getSchedule(
        @RequestHeader("Authorization") String bearerToken,
        @PathVariable("scheduleId") String scheduleId
    ) {
        List<Schedule> schedules = scheduleService.getAllSchedule(bearerToken);
        return schedules;
    }
}
