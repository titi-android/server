package com.example.busnotice.global.jwt;

import com.example.busnotice.global.exception.JwtAuthenticationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

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
        } catch (JwtAuthenticationException e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            ObjectMapper objectMapper = new ObjectMapper();
            ErrorResponse errorResponse = new ErrorResponse(401, null, e.getMessage());

            try {
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            } catch (IOException ex) {
                ex.printStackTrace();
            }

//            여기서 바로 응답하는 것 말고, AuthenticationEntryPoint 에서 처리하는 방법
//            에러 로그 보여서 사용 취소
//            catch (JwtAuthenticationException e) {
//                request.setAttribute("exceptionMessage", e.getMessage());
//                throw e;
//            }
        }

    }

    private record ErrorResponse<T>(
        int status,
        T data,
        String message
    ) {

    }


}
