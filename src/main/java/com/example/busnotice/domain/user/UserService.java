package com.example.busnotice.domain.user;

import com.example.busnotice.global.code.StatusCode;
import com.example.busnotice.global.exception.UserException;
import com.example.busnotice.global.jwt.JwtProvider;
import com.example.busnotice.global.jwt.TokenResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void signUp(String name, String password) {
        if (userRepository.existsByName(name)) {
            throw new UserException(StatusCode.CONFLICT, "이미 존재하는 이름입니다.");
        }
        userRepository.save(new User(name, passwordEncoder.encode(password)));
    }

    @Transactional
    public TokenResponse login(String name, String password) {
        User user = userRepository.findByName(name).orElseThrow(() -> new UserException(
            StatusCode.NOT_FOUND, "존재하지 않는 사용자입니다."));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserException(StatusCode.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
        }
        String accessToken = jwtProvider.createAccessToken(name);
        String refreshToken = jwtProvider.createRefreshToken();
        // 기존 리프레시 토큰 존재 시 삭제
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByUserName(name);
        if (optionalRefreshToken.isPresent()) {
            refreshTokenRepository.delete(optionalRefreshToken.get());
            refreshTokenRepository.flush();
        }
        // 리프레시 토큰 저장
        refreshTokenRepository.save(new RefreshToken(user, refreshToken));
        return new TokenResponse(accessToken, refreshToken);
    }

}
