package com.example.busnotice.global.security;

import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.user.UserRepository;
import com.example.busnotice.global.code.ErrorCode;
import com.example.busnotice.global.exception.JwtAuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
//        User user = userRepository.findByName(username)
//            .orElseThrow(() -> new UserException(StatusCode.NOT_FOUND,
//                "JWT 를 통해 추출한 유저의 이름이 DB에 존재하지 않습니다."));

        User user = userRepository.findById(Long.parseLong(userId))
            .orElseThrow(
                () -> new JwtAuthenticationException(ErrorCode.ACCESS_TOKEN_USER_NOT_FOUND));

        return new CustomUserDetails(user);
    }
}
