package com.example.busnotice.global.exception;

import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationException extends AuthenticationException {

    private final String customMessage;

    public JwtAuthenticationException(String msg) {
        super(msg);
        this.customMessage = msg;
    }

    @Override
    public String getMessage() {
        return customMessage;
    }
}
