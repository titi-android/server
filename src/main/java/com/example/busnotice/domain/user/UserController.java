package com.example.busnotice.domain.user;

import com.example.busnotice.domain.user.req.LoginRequest;
import com.example.busnotice.domain.user.req.SignUpRequest;
import com.example.busnotice.domain.user.res.RefreshTokenResponse;
import com.example.busnotice.global.format.ApiResponse;
import com.example.busnotice.global.jwt.JwtProvider;
import com.example.busnotice.global.jwt.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "유저 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    @PostMapping("/users/signup")
    @Operation(summary = "회원 가입")
    public ApiResponse<Void> signUp(
        @RequestBody SignUpRequest signUpRequest
    ) {
        userService.signUp(signUpRequest.name(), signUpRequest.password());
        return ApiResponse.createSuccess("회원가입에 성공했습니다.");
    }

    @PostMapping("/users/login")
    @Operation(summary = "로그인")
    public ApiResponse<TokenResponse> login(
        @RequestBody LoginRequest loginRequest
    ) {
        TokenResponse tokenResponse = userService.login(loginRequest.name(),
            loginRequest.password());
        return ApiResponse.createSuccessWithData(tokenResponse, "로그인에 성공했습니다.");
    }

    @PostMapping("/users/refresh")
    @Operation(summary = "엑세스 토큰 재발급")
    public ApiResponse<RefreshTokenResponse> recreateAccessToken(
        @RequestHeader("Refresh-Token") String refreshToken
    ) {
        RefreshTokenResponse refreshTokenResponse = jwtProvider.recreateAccessToken(refreshToken);
        return ApiResponse.createSuccessWithData(refreshTokenResponse,
            "엑세스 토큰이 재발급 되었습니다.");
    }
}
