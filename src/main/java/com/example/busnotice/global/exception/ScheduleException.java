package com.example.busnotice.global.exception;

import com.example.busnotice.global.code.ErrorCode;

public class ScheduleException extends RuntimeException {

    private final ErrorCode code;

    public ScheduleException(ErrorCode code) {
        this.code = code;
    }

    public ErrorCode getErrorCode() {
        return code;
    }
}