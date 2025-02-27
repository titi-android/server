package com.example.busnotice.global.exception;

import com.example.busnotice.domain.busStop.BusStop;
import com.example.busnotice.global.code.ErrorCode;
import com.example.busnotice.global.code.StatusCode;

public class BusStopException extends RuntimeException {

    private final ErrorCode code;

    public BusStopException(ErrorCode code) {
        this.code = code;
    }

    public ErrorCode getErrorCode() {
        return code;
    }
}