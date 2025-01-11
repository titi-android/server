package com.example.busnotice.domain.schedule.res;

import java.time.LocalTime;
import java.util.List;

public record ScheduleInfoResponse(
    Long id,
    String name,
    String days,
    LocalTime startTime,
    LocalTime endTime,
    String busStopName,
    List<String> busNames
) {

}
