package com.example.busnotice.global.exception;

import com.example.busnotice.global.code.ErrorCode;

public class RefreshTokenException extends RuntimeException {

    private final ErrorCode code;

    public RefreshTokenException(ErrorCode code) {
        this.code = code;
    }

    public ErrorCode getErrorCode() {
        return code;
    }
}