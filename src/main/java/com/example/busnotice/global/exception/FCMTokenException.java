package com.example.busnotice.global.exception;

import com.example.busnotice.global.code.ErrorCode;

public class FCMTokenException extends RuntimeException {


    private final ErrorCode code;

    public FCMTokenException(ErrorCode code) {
        this.code = code;
    }

    public ErrorCode getErrorCode() {
        return code;
    }

}