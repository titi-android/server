package com.example.busnotice.domain.schedule.req;

import java.time.LocalDateTime;
import java.util.List;

public record CreateScheduleRequest(
    String name, // 스케줄 이름
    LocalDateTime startTime, // 시작 시간
    LocalDateTime endTime, // 마치는 시간
    Long cityCode, // 도시 코드
    String busStopName, // 버스 정류장 이름
    List<String> busList // 버스 종류
) {

}
