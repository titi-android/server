package com.example.busnotice.global.exception;

import com.example.busnotice.domain.busStop.CityCode;
import com.example.busnotice.global.code.ErrorCode;
import com.example.busnotice.global.code.StatusCode;

public class CityCodeException extends RuntimeException {


    private final ErrorCode code;

    public CityCodeException(ErrorCode code) {
        this.code = code;
    }

    public ErrorCode getErrorCode() {
        return code;
    }

}
