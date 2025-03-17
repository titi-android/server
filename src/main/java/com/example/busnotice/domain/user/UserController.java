package com.example.busnotice.domain.user;

import com.example.busnotice.domain.user.req.FeedBackRequest;
import com.example.busnotice.domain.user.req.LoginRequest;
import com.example.busnotice.domain.user.req.SignUpRequest;
import com.example.busnotice.domain.user.res.RefreshTokenResponse;
import com.example.busnotice.global.code.ErrorCode;
import com.example.busnotice.global.format.ApiResponse;
import com.example.busnotice.global.jwt.JwtProvider;
import com.example.busnotice.global.jwt.TokenResponse;
import com.example.busnotice.global.security.CustomUserDetails;
import com.example.busnotice.util.EmailSender;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final EmailSender emailSender;

    @PostMapping("/users/signup")
    @Operation(summary = "회원 가입")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "USER401", description = "이미 존재하는 이름입니다."),
    })
    public ApiResponse<Void> signUp(
        @RequestBody SignUpRequest signUpRequest
    ) {
        userService.signUp(signUpRequest.name(), signUpRequest.password());
        return ApiResponse.createSuccess("회원가입에 성공했습니다.");
    }

    @PostMapping("/users/login")
    @Operation(summary = "로그인")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "USER402", description = "존재하지 않는 유저입니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "USER403", description = "비밀번호가 일치하지 않습니다."),
    })
    public ApiResponse<TokenResponse> login(
        @RequestBody LoginRequest loginRequest
    ) {
        TokenResponse tokenResponse = userService.login(loginRequest.name(),
            loginRequest.password());
        return ApiResponse.createSuccessWithData(tokenResponse, "로그인에 성공했습니다.");
    }

    @DeleteMapping("/users")
    @Operation(summary = "회원 탈퇴")
    public ApiResponse<String> withdrawal(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.withdrawal(userDetails.getId());
        return ApiResponse.createSuccess("회원탈퇴에 성공했습니다.");
    }

    @PostMapping("/users/refresh")
    @Operation(summary = "엑세스 토큰 재발급")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "REFRESH401", description = "리프레시 토큰이 DB에 존재하지 않습니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "REFRESH402", description = "해당 유저에 등록된 리프레시과 일치하지 않습니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "REFRESH403", description = "리프레시 토큰이 만료되었습니다."),
    })
    public ApiResponse<RefreshTokenResponse> recreateAccessToken(
        @RequestHeader("Refresh-Token") String refreshToken
    ) {
        RefreshTokenResponse refreshTokenResponse = jwtProvider.recreateAccessToken(refreshToken);
        return ApiResponse.createSuccessWithData(refreshTokenResponse,
            "엑세스 토큰이 재발급 되었습니다.");
    }

    @PostMapping("/users/feed-back")
    @Operation(summary = "문의 사항 이메일 전송")
    public ApiResponse<String> sendFeedBack(
        @RequestBody FeedBackRequest feedBackRequest,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        emailSender.sendMailNotice(userDetails.getUsername(), feedBackRequest.title(),
            feedBackRequest.content());
        return ApiResponse.createSuccess("문의 메일이 전송되었습니다.");
    }

    @GetMapping("/users/auth/validate")
    @Operation(summary = "엑세스 토큰 유효성 검증")
    public ApiResponse<String> validateAccessToken(
        HttpServletRequest request
    ) {
        boolean isValid = userService.validateAccessToken(request.getHeader("Authorization"));
        return isValid ?
            ApiResponse.createSuccess("유효한 엑세스 토큰입니다.")
            : ApiResponse.createFail(ErrorCode.ACCESS_TOKEN_VALIDATION_FAIL);
    }
}
