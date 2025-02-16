//package com.example.busnotice.global.code;
//
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import org.springframework.http.HttpStatus;
//
//@Getter
//@AllArgsConstructor
//public enum ErrorCode {
//
//    // USER
//    USER_DUPLICATED_NAME(HttpStatus.CONFLICT, "USER_DUPLICATED_NAME", "이미 존재하는 이름입니다."),
//    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "이미 존재하는 이름입니다."),
//    USER_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "USER_INVALID_PASSWORD", "비밀번호가 일치하지 않습니다."),
//
//    // REFRESH_TOKEN
//    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "REFRESH_TOKEN_NOT_FOUND", "리프레시 토큰이 DB에 존재하지 않습니다."),
//    REFRESH_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "REFRESH_TOKEN_INVALID", "해당 유저에 등록된 리프레시과 일치하지 않습니다."),
//    REFRESH_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "REFRESH_TOKEN_EXPIRED", "리프레시 토큰이 만료되었습니다."),
//
//    // REGION_NAME
//    REGION_NAME_NOT_FOUND(HttpStatus.BAD_REQUEST, "REGION_NAME_INVALID", "해당 이름의 지역이 존재하지 않습니다."),
//
//    // BUS_STOP
//    BUS_STOP_NOT_FOUND(HttpStatus.BAD_REQUEST, "BUS_STOP_NOT_FOUND", "해당 이름을 포함하는 버스정류장이 존재하지 않습니다."),
//
//
//
//    private final HttpStatus httpStatus;
//    private final String code;
//    private final String message;
//    }
//}