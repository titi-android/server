package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.schedule.req.CreateScheduleRequest;
import com.example.busnotice.domain.schedule.req.UpdateScheduleRequest;
import com.example.busnotice.domain.schedule.res.ScheduleResponse;
import com.example.busnotice.domain.schedule.res.ScheduleResponses;
import com.example.busnotice.global.format.ApiResponse;
import com.example.busnotice.global.security.CustomUserDetails;
import java.io.UnsupportedEncodingException;
import java.util.List;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/v1/schedules")
    @Description("스케줄 등록")
    public ApiResponse<Void> createSchedule(
        @RequestBody CreateScheduleRequest createScheduleRequest,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws UnsupportedEncodingException {
        scheduleService.createSchedule(userDetails.getId(), createScheduleRequest);
        return ApiResponse.createSuccess("스케줄이 생성되었습니다.");
    }

    @PutMapping("/v1/schedules/{scheduleId}")
    @Description("스케줄 수정")
    public ApiResponse<Void> updateSchedule(
        @PathVariable("scheduleId") Long scheduleId,
        @RequestBody UpdateScheduleRequest updateScheduleRequest,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws UnsupportedEncodingException {
        scheduleService.updateSchedule(userDetails.getId(), scheduleId, updateScheduleRequest);
        return ApiResponse.createSuccess("스케줄이 수정되었습니다.");
    }

    @DeleteMapping("/v1/schedules/{scheduleId}")
    @Description("스케줄 삭제")
    public ApiResponse<Void> deleteSchedule(
        @PathVariable("scheduleId") Long scheduleId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws UnsupportedEncodingException {
        scheduleService.deleteSchedule(userDetails.getId(), scheduleId);
        return ApiResponse.createSuccess("스케줄이 삭제되었습니다.");
    }


    @GetMapping("/v1/schedules/today")
    @Description("오늘 모든 스케줄의 가장 빠른 버스 정보 조회")
    public ApiResponse<List<ScheduleResponse>> getAllSchedules(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws UnsupportedEncodingException {
        List<ScheduleResponse> scheduleResponses = scheduleService.오늘_스케줄들의_가장_빨리_도착하는_버스_정보(
            userDetails.getId());
        return ApiResponse.createSuccessWithData(scheduleResponses);
    }

    @GetMapping("/v2/schedules/today")
    @Description("오늘 모든 스케줄의 가장 빠른 첫번째, 두번째 버스 정보 조회")
    public ApiResponse<List<ScheduleResponses>> getAllSchedulesV2(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws UnsupportedEncodingException {
        List<ScheduleResponses> scheduleResponsesList = scheduleService.오늘_스케줄들의_가장_빨리_도착하는_첫번째_두번째_버스_정보(
            userDetails.getId());
        return ApiResponse.createSuccessWithData(scheduleResponsesList);
    }

    @GetMapping("/v1/schedules/now")
    @Description("현재 스케줄의 가장 빠른 버스 정보 조회")
    public ApiResponse<ScheduleResponse> getCurrentSchedule(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws UnsupportedEncodingException {
        ScheduleResponse scheduleResponse = scheduleService.현재_스케줄의_가장_빨리_도착하는_버스_정보(
            userDetails.getId());
        return ApiResponse.createSuccessWithData(scheduleResponse);
    }

    @GetMapping("/v2/schedules/now")
    @Description("현재 스케줄의 가장 빠른 첫번째, 두번째 버스 정보 조회")
    public ApiResponse<ScheduleResponses> getCurrentScheduleV2(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws UnsupportedEncodingException {
        ScheduleResponses scheduleResponses = scheduleService.현재_스케줄의_가장_빨리_도착하는_첫번째_두번째_버스_정보(
            userDetails.getId());
        return ApiResponse.createSuccessWithData(scheduleResponses);
    }
}
