package com.example.busnotice.domain.user;

import com.example.busnotice.global.code.ErrorCode;
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
            RefreshToken existRefreshToken = optionalRefreshToken.get();
            existRefreshToken.update(refreshToken);
            return new TokenResponse(accessToken, refreshToken);
        }
        // 그렇지 않으면 새 리프레시 토큰 생성하여 저장
        refreshTokenRepository.save(new RefreshToken(user, refreshToken));
        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public void updateProfile(Long userId, String name) {
        User user = userRepository.findById(userId).get();
        Optional<User> anotherUser = userRepository.findByNameWithoutMe(userId, name);
        if (anotherUser.isPresent()) {
            throw new UserException(ErrorCode.USER_DUPLICATED_NAME);
        }
        user.updateName(name);
    }

    @Transactional
    public void withdrawal(Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
            new UserException(ErrorCode.USER_NOT_FOUND)
        );
        userRepository.deleteById(userId);
    }
}
