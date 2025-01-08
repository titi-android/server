package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.schedule.req.CreateScheduleRequest;
import com.example.busnotice.domain.schedule.res.ScheduleResponse;
import com.example.busnotice.global.format.ApiResponse;
import java.io.UnsupportedEncodingException;
import java.util.List;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ApiResponse<Void> createSchedule(
        @RequestBody CreateScheduleRequest createScheduleRequest,
        @RequestHeader("Authorization") String bearerToken
    ) throws UnsupportedEncodingException {
        scheduleService.createSchedule(bearerToken, createScheduleRequest);
        return ApiResponse.createSuccess("스케줄이 생성되었습니다.");
    }

    @GetMapping("/schedule/today")
    @Description("오늘 모든 스케줄의 가장 빠른 버스 정보 조회")
    public ApiResponse<List<ScheduleResponse>> getAllSchedules(
        @RequestHeader("Authorization") String bearerToken
    ) throws UnsupportedEncodingException {
        List<ScheduleResponse> scheduleResponses = scheduleService.오늘_스케줄들의_가장_빨리_도착하는_버스_정보(
            bearerToken);
        return ApiResponse.createSuccessWithData(scheduleResponses);
    }

    @GetMapping("/schedule/now")
    @Description("현재 스케줄의 가장 빠른 버스 정보 조회")
    public ApiResponse<ScheduleResponse> getCurrentSchedule(
        @RequestHeader("Authorization") String bearerToken
    ) throws UnsupportedEncodingException {
        ScheduleResponse scheduleResponse = scheduleService.현재_스케줄의_가장_빨리_도착하는_버스_정보(
            bearerToken);
        return ApiResponse.createSuccessWithData(scheduleResponse);
    }
}
