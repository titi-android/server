package com.example.busnotice.global.format;

import com.example.busnotice.global.code.StatusCode;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private boolean isSuccess;
    private int code;
    private T data;
    private String message;

    public static <T> ApiResponse<T> createSuccess() {
        return new ApiResponse<>(true, StatusCode.OK);
    }

    public static <T> ApiResponse<T> createSuccess(String message) {
        return new ApiResponse<>(true, StatusCode.OK, message);
    }

    public static <T> ApiResponse<T> createSuccessWithData(T data) {
        return new ApiResponse<>(true, StatusCode.OK, data);
    }

    public static <T> ApiResponse<T> createSuccessWithData(T data, String message) {
        return new ApiResponse<>(true, StatusCode.OK, data, message);
    }

    public static <T> ApiResponse<T> createFail(StatusCode status, String message) {
        return new ApiResponse<>(false, status, message);
    }

    private ApiResponse(Boolean isSuccess, StatusCode status) {
        this.isSuccess = isSuccess;
        this.code = status.getCode();
        this.data = null;
        this.message = null;
    }

    private ApiResponse(Boolean isSuccess, StatusCode status, String message) {
        this.isSuccess = isSuccess;
        this.code = status.getCode();
        this.data = null;
        this.message = message;
    }

    private ApiResponse(Boolean isSuccess, StatusCode status, T data) {
        this.isSuccess = isSuccess;
        this.code = status.getCode();
        this.data = data;
        this.message = null;
    }

    private ApiResponse(Boolean isSuccess, StatusCode status, T data, String message) {
        this.isSuccess = isSuccess;
        this.code = status.getCode();
        this.data = data;
        this.message = message;
    }

}
