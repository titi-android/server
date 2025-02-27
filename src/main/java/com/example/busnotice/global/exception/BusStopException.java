package com.example.busnotice.global.exception;

import com.example.busnotice.global.code.ErrorCode;

public class BusStopException extends RuntimeException {

    private final ErrorCode code;

    public BusStopException(ErrorCode code) {
        this.code = code;
    }

    public ErrorCode getErrorCode() {
        return code;
    }
}