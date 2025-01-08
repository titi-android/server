package com.example.busnotice.global.code;

public enum StatusCode {
    // 성공 응답
    OK(200),
    CREATED(201),
    NO_CONTENT(204),

    // 클라이언트 오류 응답
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409),

    // 서버 오류 응답
    INTERNAL_SERVER_ERROR(500),
    SERVICE_UNAVAILABLE(503);

    private final int code;

    StatusCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
