package com.example.busnotice.global.handler;

import com.example.busnotice.global.exception.BusinessException;
import com.example.busnotice.global.exception.schedule.ScheduleException;
import com.example.busnotice.global.format.ApiResponse;
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
}
