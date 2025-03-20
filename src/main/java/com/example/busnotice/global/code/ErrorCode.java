package com.example.busnotice.global.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // USER
    USER_DUPLICATED_NAME(HttpStatus.CONFLICT, "USER401", "이미 존재하는 이름입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER402", "존재하지 않는 유저입니다."),
    USER_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "USER403", "비밀번호가 일치하지 않습니다."),
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "USER404", "사용자의 권한이 없습니다."),

    // REFRESH_TOKEN
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "REFRESH401", "리프레시 토큰이 DB에 존재하지 않습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "REFRESH402", "해당 유저에 등록된 리프레시과 일치하지 않습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "REFRESH403", "리프레시 토큰이 만료되었습니다."),

    // REGION_NAME
    CITY_CODE_NOT_FOUND(HttpStatus.BAD_REQUEST, "CITY_CODE401", "해당 이름과 매칭되는 도시코드가 존재하지 않습니다."),

    // BUS_STOP
    BUS_STOP_NOT_FOUND(HttpStatus.BAD_REQUEST, "BUS_STOP401", "해당 이름을 포함하는 버스정류장이 존재하지 않습니다."),

    // SCHEDULE
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE401", "해당 ID의 스케줄이 존재하지 않습니다."),
    SCHEDULE_CONFLICT(HttpStatus.CONFLICT, "SCHEDULE402", "겹치는 스케줄이 존재합니다."),

    // ACCESS_TOKEN
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT401", "엑세스 토큰이 만료되었습니다."),
    ACCESS_TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "JWT402", "엑세스 토큰의 서명이 유효하지 않습니다."),
    ACCESS_TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "JWT403", "엑세스 토큰 형식이 올바르지 않습니다."),
    ACCESS_TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "JWT404", "지원되지 않는 엑세스 토큰입니다."),
    ACCESS_TOKEN_ILLEGAL_ARGUMENT(HttpStatus.UNAUTHORIZED, "JWT405", "잘못된 엑세스 토큰입니다."),
    ACCESS_TOKEN_USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "JWT406",
        "JWT 를 통해 추출한 유저의 ID가 DB에 존재하지 않습니다."),
    ACCESS_TOKEN_VALIDATION_FAIL(HttpStatus.UNAUTHORIZED, "JWT407", "(통합) 유효성 검증에 실패한 엑세스 토큰입니다."),

    // FCM_TOKEN
    FCM_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "FCM401", "해당 유저에 등록된 FCM 토큰이 존재하지 않습니다."),


    // ETC
    UNSUPPORTED_ENCODING(HttpStatus.INTERNAL_SERVER_ERROR, "UNSUPPORTED_ENCODING",
        "지원되지 않는 인코딩 형식입니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}