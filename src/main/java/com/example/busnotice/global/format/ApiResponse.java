package com.example.busnotice.global.format;

import com.example.busnotice.global.code.StatusCode;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private StatusCode status;
    private T data;
    private String message;

    public static <T> ApiResponse<T> createSuccess() {
        return new ApiResponse<>(StatusCode.OK);
    }

    public static <T> ApiResponse<T> createSuccess(String message) {
        return new ApiResponse<>(StatusCode.OK, message);
    }

    public static <T> ApiResponse<T> createSuccessWithData(T data) {
        return new ApiResponse<>(StatusCode.OK, data);
    }

    public static <T> ApiResponse<T> createSuccessWithData(T data, String message) {
        return new ApiResponse<>(StatusCode.OK, data, message);
    }

    public static <T> ApiResponse<T> createFail(StatusCode status, String message) {
        return new ApiResponse<>(status, message);
    }

    private ApiResponse(StatusCode status) {
        this.status = status;
        this.data = null;
        this.message = null;
    }

    private ApiResponse(StatusCode status, String message) {
        this.status = status;
        this.data = null;
        this.message = message;
    }

    private ApiResponse(StatusCode status, T data) {
        this.status = status;
        this.data = data;
        this.message = null;
    }

    private ApiResponse(StatusCode status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

}
