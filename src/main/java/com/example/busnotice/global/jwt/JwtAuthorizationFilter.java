package com.example.busnotice.global.jwt;

import com.example.busnotice.global.exception.JwtAuthenticationException;
import com.example.busnotice.global.security.CustomAuthenticationEntryPoint;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomAuthenticationEntryPoint entryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        // JWT 필터 거치는 PATH 인지 검사
        List<String> excludePaths = Arrays.asList(
            "/api/v1/users/signup",
            "/api/v1/users/login",
            "/api/v1/users/refresh",
            "/h2-console",
            "/swagger-ui", "/swagger-resource", "/v3/api-docs"
        );
        String requestPath = request.getRequestURI();
        if (excludePaths.stream().anyMatch(requestPath::contains)) {
            filterChain.doFilter(request, response);
            return;
        }
        // 검증 거쳐야하는 PATH 인 경우
        System.out.println("검증 필요");

        try {
            String bearerToken = request.getHeader("Authorization");
            String token = jwtProvider.extractToken(bearerToken);

            Authentication authentication = jwtProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (JwtAuthenticationException e) {  // ✅ 더 구체적인 예외를 먼저 처리
            System.out.println("🔥 JwtAuthenticationException 잡힘: " + e.getMessage());
            request.setAttribute("exceptionMessage", e.getMessage());
            entryPoint.commence(request, response, e);
        } catch (AuthenticationException e) { // 인증 관련 예외 잡기
            request.setAttribute("exceptionMessage", e.getMessage());
            SecurityContextHolder.clearContext();

            // AuthenticationEntryPoint 직접 호출하여 예외 처리 (throw 하지 않음)
            entryPoint.commence(request, response, e);
        }
    }
}

