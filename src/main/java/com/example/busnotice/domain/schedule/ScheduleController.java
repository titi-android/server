package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.schedule.req.CreateScheduleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> createSchedule(
        @RequestBody CreateScheduleRequest createScheduleRequest,
        @RequestHeader("Authorization") String bearerToken
    ){
        Schedule schedule = scheduleService.createSchedule(bearerToken, createScheduleRequest);
        return ResponseEntity.status(HttpStatus.OK).body(schedule.toString());
    }
}
