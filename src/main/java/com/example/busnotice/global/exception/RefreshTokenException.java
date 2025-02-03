package com.example.busnotice.global.exception;

import com.example.busnotice.global.code.StatusCode;

public class RefreshTokenException extends RuntimeException {

    private final StatusCode statusCode;

    public RefreshTokenException(StatusCode status, String message) {
        super(message);
        this.statusCode = status;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }
}
