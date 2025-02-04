package com.example.busnotice.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public CustomAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver")
    HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) {
//        resolver.resolveException(request, response, null, authException);

        String message = (String) request.getAttribute("exceptionMessage");
        if (message == null) {
            message = "JWT 토큰 인증에 실패했습니다";
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            response.getWriter()
                .write("{\"status\": 401, \"data\": null, \"message\": \"" + message + "\"}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
