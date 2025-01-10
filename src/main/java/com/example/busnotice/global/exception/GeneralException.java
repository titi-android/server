package com.example.busnotice.global.exception;

import com.example.busnotice.global.code.StatusCode;

public class GeneralException extends RuntimeException {

    private final StatusCode statusCode;

    public GeneralException(StatusCode status, String message) {
        super(message);
        this.statusCode = status;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }
}
