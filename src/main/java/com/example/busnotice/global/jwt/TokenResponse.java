package com.example.busnotice.global.jwt;

public record TokenResponse(
    String accessToken,
    String refreshToken
) {

}
