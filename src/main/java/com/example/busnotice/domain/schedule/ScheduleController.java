package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.schedule.req.CreateScheduleRequest;
//import com.example.busnotice.domain.schedule.req.UpdateScheduleRequest;
//import com.example.busnotice.domain.schedule.res.ScheduleInfoResponse;
//import com.example.busnotice.domain.schedule.res.ScheduleResponse;
import com.example.busnotice.domain.schedule.req.UpdateScheduleRequest;
import com.example.busnotice.global.format.ApiResponse;
import com.example.busnotice.global.security.CustomUserDetails;
import com.example.busnotice.util.DayConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "Schedule", description = "스케줄 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/v2/schedules")
    @Operation(summary = "스케줄 등록")
    public ApiResponse<Void> createScheduleV2(
            @RequestBody CreateScheduleRequest createScheduleRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        scheduleService.createSchedule(userDetails.getId(), createScheduleRequest);
        return ApiResponse.createSuccess("스케줄이 생성되었습니다.");
    }

//    @GetMapping("/v1/schedules/{scheduleId}")
//    @Operation(
//            summary = "스케줄 조회"
//    )
//    public ApiResponse<ScheduleInfoResponse> getSchedule(
//            @PathVariable("scheduleId") Long scheduleId,
//            @AuthenticationPrincipal CustomUserDetails userDetails
//    ) {
//        ScheduleInfoResponse scheduleInfoResponse = scheduleService.getSchedule(userDetails.getId(),
//                scheduleId);
//        return ApiResponse.createSuccessWithData(scheduleInfoResponse, "스케줄 조회에 성공했습니다.");
//
//    }
//
    @PutMapping("/v2/schedules/{scheduleId}")
    @Operation(summary = "스케줄 수정")
    public ApiResponse<Void> updateSchedule(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestBody UpdateScheduleRequest updateScheduleRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        scheduleService.updateSchedule(userDetails.getId(), scheduleId, updateScheduleRequest);
        return ApiResponse.createSuccess("스케줄이 수정되었습니다.");
    }
//
//
//    @DeleteMapping("/v1/schedules/{scheduleId}")
//    @Operation(summary = "스케줄 삭제")
//    public ApiResponse<Void> deleteSchedule(
//            @PathVariable("scheduleId") Long scheduleId,
//            @AuthenticationPrincipal CustomUserDetails userDetails
//    ) {
//        scheduleService.deleteSchedule(userDetails.getId(), scheduleId);
//        return ApiResponse.createSuccess("스케줄이 삭제되었습니다.");
//    }
//
//    @PutMapping("/v1/schedules/alarm/{scheduleId}")
//    @Operation(summary = "스케줄 알림 여부 수정")
//    public ApiResponse<Void> updateAlarm(
//            @PathVariable("scheduleId") Long scheduleId,
//            @AuthenticationPrincipal CustomUserDetails userDetails
//    ) {
//        boolean isAlarmOn = scheduleService.updateAlarm(userDetails.getId(), scheduleId);
//        String msg = isAlarmOn ? "알림이 켜졌습니다." : "알림이 꺼졌습니다.";
//        return ApiResponse.createSuccess(msg);
//    }
//
//    @GetMapping("/v2/schedules/today")
//    @Operation(
//            summary = "오늘 모든 스케줄의 가장 빠른 첫번째, 두번째 버스 정보 조회",
//            description = "오늘 스케줄이 없는 경우 빈 리스트를 반환"
//    )
//    public ApiResponse<List<ScheduleResponse>> getAllSchedulesOfTodayV2(
//            @AuthenticationPrincipal CustomUserDetails userDetails
//    ) throws UnsupportedEncodingException {
//        System.out.println("DayConverter.getTodayAsString() = " + DayConverter.getTodayAsString());
//        List<ScheduleResponse> scheduleResponsesList = scheduleService.특정_요일의_스케줄들의_가장_빨리_도착하는_첫번째_두번째_버스_정보(
//                userDetails.getId(), DayConverter.getTodayAsString());
//        String msg = scheduleResponsesList.isEmpty()
//                ? "오늘 스케줄이 존재하지 않습니다."
//                : "오늘 스케줄이 존재합니다.";
//        return ApiResponse.createSuccessWithData(scheduleResponsesList, msg);
//    }
//
//    @GetMapping("/v2/schedules/days")
//    @Operation(
//            summary = "특정 요일의 모든 스케줄의 가장 빠른 첫번째, 두번째 버스 정보 조회",
//            description = "오늘 스케줄이 없는 경우 빈 리스트를 반환"
//    )
//    public ApiResponse<List<ScheduleResponse>> getAllSchedulesOfDaysV2(
//            @RequestParam("days") String days,
//            @AuthenticationPrincipal CustomUserDetails userDetails
//    ) throws UnsupportedEncodingException {
//        List<ScheduleResponse> scheduleResponses = scheduleService.특정_요일의_스케줄들의_가장_빨리_도착하는_첫번째_두번째_버스_정보(
//                userDetails.getId(), days);
//        String msg = scheduleResponses.isEmpty()
//                ? days + " 스케줄이 존재하지 않습니다."
//                : days + " 스케줄이 존재합니다.";
//        return ApiResponse.createSuccessWithData(scheduleResponses, msg);
//    }
//
//    @GetMapping("/v2/schedules/now")
//    @Operation(
//            summary = "현재 스케줄의 가장 빠른 첫번째, 두번째 버스 정보 조회",
//            description = "현재 스케줄이 없는 경우 null 을 반환"
//    )
//    public ApiResponse<ScheduleResponse> getCurrentScheduleV2(
//            @AuthenticationPrincipal CustomUserDetails userDetails
//    ) throws UnsupportedEncodingException {
//        ScheduleResponse scheduleResponse = scheduleService.현재_스케줄의_가장_빨리_도착하는_첫번째_두번째_버스_정보(
//                userDetails.getId());
//        String msg = (scheduleResponse == null)
//                ? "현재 스케줄이 존재하지 않습니다."
//                : "현재 스케줄이 존재합니다.";
//        return ApiResponse.createSuccessWithData(scheduleResponse, msg);
//    }
}
