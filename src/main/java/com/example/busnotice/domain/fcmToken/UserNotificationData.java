package com.example.busnotice.domain.fcmToken;

import java.util.List;

public record UserNotificationData(
    String token,
    String scheduleName,
    List<String> days,
    String busStopName,
    String firstBusName,
    int firstArrPrevStCnt,
    int firstArrTime,
    String secondBusName,
    int secondArrPrevStCnt,
    int secondArrTime
) {

}
