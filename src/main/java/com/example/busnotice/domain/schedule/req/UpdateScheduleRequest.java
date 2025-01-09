package com.example.busnotice.domain.schedule.req;

import java.time.LocalTime;
import java.util.List;

public record UpdateScheduleRequest(
    String name, // 스케줄 이름
    String days, // 요일
    LocalTime startTime, // 시작 시간
    LocalTime endTime, // 마치는 시간
    String regionName, // 지역 이름
    String busStopName, // 버스 정류장 이름
    List<String> busList // 버스 종류
) {

}
