package com.example.busnotice.global.exception;

import com.example.busnotice.global.code.StatusCode;

public class BusStopException extends RuntimeException{

    private final StatusCode statusCode;

    public BusStopException(StatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }
}
