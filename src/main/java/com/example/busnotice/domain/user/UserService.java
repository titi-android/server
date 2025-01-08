package com.example.busnotice.domain.user;

import com.example.busnotice.global.code.StatusCode;
import com.example.busnotice.global.exception.UserException;
import com.example.busnotice.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public void signUp(String name, String password) {
        if (userRepository.existsByName(name)) {
            throw new UserException(StatusCode.CONFLICT, "이미 존재하는 이름입니다.");
        }

        User newUser = User.builder()
            .name(name)
            .password(password)
            .build();

        userRepository.save(newUser);
    }

    public String login(String name, String password) {
        User user = userRepository.findByName(name).orElseThrow(() -> new UserException(
            StatusCode.BAD_REQUEST, "로그인 정보가 올바르지 않습니다."));
        if (!user.getPassword().equals(password)) {
            throw new UserException(StatusCode.BAD_REQUEST, "로그인 정보가 올바르지 않습니다.");
        }

        String jwt = jwtProvider.createToken(name);
        return jwt;
    }

}
