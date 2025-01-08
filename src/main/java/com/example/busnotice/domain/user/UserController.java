package com.example.busnotice.domain.user;

import com.example.busnotice.domain.user.req.LoginRequest;
import com.example.busnotice.domain.user.req.SignUpRequest;
import com.example.busnotice.global.format.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    @PostMapping("/users/signup")
    public ApiResponse<Void> signUp(
        @RequestBody SignUpRequest signUpRequest
    ) {
        userService.signUp(signUpRequest.name(), signUpRequest.password());
        return ApiResponse.createSuccess("회원가입에 성공했습니다.");
    }

    @PostMapping("/users/login")
    public ApiResponse<String> login(
        @RequestBody LoginRequest loginRequest
    ) {
        String jwt = userService.login(loginRequest.name(), loginRequest.password());
        return ApiResponse.createSuccessWithData(jwt, "로그인에 성공했습니다.");
    }
}
