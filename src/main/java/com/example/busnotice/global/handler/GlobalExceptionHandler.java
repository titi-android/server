package com.example.busnotice.global.handler;

import com.example.busnotice.global.code.StatusCode;
import com.example.busnotice.global.exception.BusStopException;
import com.example.busnotice.global.exception.BusinessException;
import com.example.busnotice.global.exception.GeneralException;
import com.example.busnotice.global.exception.ScheduleException;
import com.example.busnotice.global.exception.UserException;
import com.example.busnotice.global.format.ApiResponse;
import java.io.UnsupportedEncodingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        return ApiResponse.createFail(e.getStatusCode(), e.getMessage());
    }

    @ExceptionHandler(ScheduleException.class)
    public ApiResponse<Void> handleScheduleException(ScheduleException e) {
        return ApiResponse.createFail(e.getStatusCode(), e.getMessage());
    }

    @ExceptionHandler(UserException.class)
    public ApiResponse<Void> handleUserException(UserException e) {
        return ApiResponse.createFail(e.getStatusCode(), e.getMessage());
    }

    @ExceptionHandler(BusStopException.class)
    public ApiResponse<Void> busStopException(BusStopException e) {
        return ApiResponse.createFail(e.getStatusCode(), e.getMessage());
    }

    @ExceptionHandler(UnsupportedEncodingException.class)
    public ApiResponse<Void> handleUnsupportedEncodingException(UnsupportedEncodingException e) {
        return ApiResponse.createFail(StatusCode.INTERNAL_SERVER_ERROR, "지원되지 않는 인코딩을 사용하였습니다.");
    }

    @ExceptionHandler(GeneralException.class)
    public ApiResponse<Void> GeneralException(GeneralException e) {
        return ApiResponse.createFail(StatusCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
