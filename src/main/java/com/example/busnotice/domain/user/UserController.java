package com.example.busnotice.domain.user;

import com.example.busnotice.domain.user.request.LoginRequest;
import com.example.busnotice.domain.user.request.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> signUp(
        @RequestBody SignUpRequest signUpRequest
    ) {
        ResponseEntity<String> res = userService.signUp(signUpRequest.name(),
            signUpRequest.password());
        return res;
    }

    @PostMapping("/users/login")
    public ResponseEntity<String> login(
        @RequestBody LoginRequest loginRequest
    ) {
        ResponseEntity<String> res = userService.login(loginRequest.name(),
            loginRequest.password());
        return res;
    }
}
