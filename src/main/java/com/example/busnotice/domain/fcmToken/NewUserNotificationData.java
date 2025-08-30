package com.example.busnotice.domain.fcmToken;

import java.util.List;

public record NewUserNotificationData(
        String token,
        Long scheduleId,
        String scheduleName,
        List<String> days,
        List<SimpleSectionDto> sections // title, detail만
) {
    public record SimpleSectionDto(
            String title,   // ex) [버스] ○○정류장,  [지하철] 2호선 강남(상행)
            String detail   // ex) 버스 도착/지연/남은 정거장 등, 지하철 도착·도착예정 등
    ) {}
}