package com.example.busnotice.global.exception;

import com.example.busnotice.global.code.StatusCode;

public class CityCodeException extends RuntimeException {


    private final StatusCode statusCode;

    public CityCodeException(StatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

}
