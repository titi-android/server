package com.example.busnotice.domain.fcmToken;

import com.example.busnotice.domain.fcmToken.req.CreateFCMTokenRequest;
import com.example.busnotice.global.format.ApiResponse;
import com.example.busnotice.global.security.CustomUserDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
@RestController
public class FCMController {

    private final FCMService fcmService;

    @PostMapping("/api/v1/fcm/tokens")
    @Operation(summary = "FCM 토큰 등록")
    public ApiResponse<String> createFCMToken(
            @RequestBody CreateFCMTokenRequest createFCMTokenRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        fcmService.createFCMToken(userDetails.getId(), createFCMTokenRequest);
        return ApiResponse.createSuccess("FCM 토큰이 등록되었습니다.");
    }

    @DeleteMapping("/api/v1/fcm/tokens")
    @Operation(summary = "FCM 토큰 삭제")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "FCM401", description = "해당 유저에 등록된 FCM 토큰이 존재하지 않습니다."),
    })
    public ApiResponse<String> deleteFCMToken(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        fcmService.deleteFCMToken(userDetails.getId());
        return ApiResponse.createSuccess("FCM 토큰이 삭제되었습니다.");
    }

    @GetMapping("/api/v1/fcm/test")
    @Operation(summary = "FCM 토큰 전송 테스트, 현재 스케줄이 있는 모든 유저에게 알림 전송")
    public ApiResponse<String> sendTestNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws UnsupportedEncodingException, JsonProcessingException {
        fcmService.sendTestNotification();
        return ApiResponse.createSuccess("테스트 FCM 이 전송되었습니다.");
    }
}
