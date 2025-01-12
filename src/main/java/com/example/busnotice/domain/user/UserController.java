package com.example.busnotice.domain.user;

import com.example.busnotice.domain.user.req.LoginRequest;
import com.example.busnotice.domain.user.req.SignUpRequest;
import com.example.busnotice.global.format.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "유저 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

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
    public ApiResponse<String> login(
        @RequestBody LoginRequest loginRequest
    ) {
        String jwt = userService.login(loginRequest.name(), loginRequest.password());
        return ApiResponse.createSuccessWithData(jwt, "로그인에 성공했습니다.");
    }
}
