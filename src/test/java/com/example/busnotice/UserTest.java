package com.example.busnotice;

import com.example.busnotice.domain.user.UserRepository;
import com.example.busnotice.domain.user.UserService;
import com.example.busnotice.global.jwt.JwtProvider;
import com.example.busnotice.global.jwt.TokenResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class UserTest {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProvider jwtProvider;

    String name = "donghyun";
    String password = "1234";

    @Test
    public void SignUpSuccess() {
        userService.signUp(name, password);
    }

    @Test
    public void LoginSuccess() {
        userService.signUp(name, password);
        TokenResponse tr = userService.login(name, password);
        System.out.println("tr = " + tr);
    }

    @Disabled
    @Test
    public void getUsernameFromJwt() {
        userService.signUp(name, password);
        TokenResponse tr = userService.login(name, password);

        Assertions.assertThat(name).isEqualTo(jwtProvider.getUserId(tr.accessToken()));
    }
}
