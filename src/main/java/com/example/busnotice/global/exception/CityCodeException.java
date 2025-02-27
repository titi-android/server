package com.example.busnotice.global.exception;

import com.example.busnotice.global.code.ErrorCode;

public class CityCodeException extends RuntimeException {


    private final ErrorCode code;

    public CityCodeException(ErrorCode code) {
        this.code = code;
    }

    public ErrorCode getErrorCode() {
        return code;
    }

}