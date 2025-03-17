package com.example.busnotice.domain.user;

import com.example.busnotice.global.code.ErrorCode;
import com.example.busnotice.global.exception.JwtAuthenticationException;
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
            throw new UserException(ErrorCode.USER_DUPLICATED_NAME);
        }
        userRepository.save(new User(name, passwordEncoder.encode(password)));
    }

    @Transactional
    public TokenResponse login(String name, String password) {
        User user = userRepository.findByName(name)
            .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserException(ErrorCode.USER_INVALID_PASSWORD);
        }
        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken();
        // 기존 리프레시 토큰 존재 시 업데이트
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByUserName(name);
        if (optionalRefreshToken.isPresent()) {
            optionalRefreshToken.get().update(refreshToken);
            return new TokenResponse(accessToken, refreshToken);
        }
        // 리프레시 토큰 기존에 없다면 생성하여 저장
        refreshTokenRepository.save(new RefreshToken(user, refreshToken));
        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public void withdrawal(Long userId) {
        userRepository.deleteById(userId);
    }

    public boolean isValidAccessToken(String bearerToken) {
        try {
            String token = jwtProvider.extractToken(bearerToken);
            jwtProvider.validateAccessToken(token);
            return true;
        } catch (JwtAuthenticationException e) {
            return false;
        }
    }
}
