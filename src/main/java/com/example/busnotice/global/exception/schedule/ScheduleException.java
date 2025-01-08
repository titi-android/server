package com.example.busnotice.global.exception.schedule;

import com.example.busnotice.global.code.StatusCode;

public class ScheduleException extends RuntimeException {

    private final StatusCode statusCode;

    public ScheduleException(StatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }
}
