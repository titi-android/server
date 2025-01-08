package com.example.busnotice.global.exception;

import com.example.busnotice.global.code.StatusCode;

public class BusinessException extends RuntimeException {

    private final StatusCode statusCode;

    private BusinessException(StatusCode status, String message) {
        super(message);
        this.statusCode = status;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }
}
