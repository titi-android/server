package com.example.busnotice.domain.fcmToken;

public record UserNotificationData(
    String token,
    String scheduleName,
    String days,
    String busStopName,
    String firstBusName,
    int firstArrPrevStCnt,
    int firstArrTime,
    String secondBusName,
    int secondArrPrevStCnt,
    int secondArrTime
) {}
