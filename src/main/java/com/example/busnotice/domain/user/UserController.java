package com.example.busnotice.domain.user;

import com.example.busnotice.domain.user.request.SignUpRequest;
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

    @PostMapping("/user")
    public String signUp(
        @RequestBody SignUpRequest signUpRequest
    ){
        return "회원가입 완료";
    }
}
