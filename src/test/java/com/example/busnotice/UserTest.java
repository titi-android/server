package com.example.busnotice;

import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.user.UserRepository;
import com.example.busnotice.domain.user.UserService;
import com.example.busnotice.global.jwt.JwtProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class UserTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtProvider jwtProvider;

    String name = "donghyun";
    String password = "1234";

    @Test
    public void SignUpSuccess() {
        ResponseEntity<String> res = userService.signUp(name, password);
        System.out.println("response = " + res);
    }
    @Test
    public void SignUpFailed() {
        userRepository.save(new User(name, password));

        ResponseEntity<String> res = userService.signUp(name, password);
        System.out.println("response = " + res);
    }

    @Test
    public void LoginSuccess() {
        userService.signUp(name, password);
        ResponseEntity<String> res = userService.login(name, password);
        System.out.println("response = " + res);
    }

    @Test
    public void getUsernameFromJwt() {
        userService.signUp(name, password);
        ResponseEntity<String> res = userService.login(name, password);
        String jwt = res.getBody().toString();

        Assertions.assertThat(name).isEqualTo(jwtProvider.getUsername(jwt));
    }
}
