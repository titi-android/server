package com.example.busnotice.domain.user;

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

    public ResponseEntity<String> signUp(String name, String password) {
        if (userRepository.existsByName(name)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 존재하는 이름입니다.");
        }

        User newUser = User.builder()
            .name(name)
            .password(password)
            .build();

        userRepository.save(newUser);
        return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");
    }

    public ResponseEntity<String> login(String name, String password) {
        if (!userRepository.existsByName(name)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("존재하지 않는 회원입니다.");
        }
        User user = userRepository.findByName(name);
        if (!user.getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("비밀번호가 틀렸습니다.");
        }

        String token = jwtProvider.createToken(name);
        return ResponseEntity.status(HttpStatus.OK).body(token.toString());
    }

}
