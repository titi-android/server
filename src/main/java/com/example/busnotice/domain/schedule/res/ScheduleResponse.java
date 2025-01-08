package com.example.busnotice.domain.schedule.res;

import java.time.LocalTime;

public record ScheduleResponse(
    String days,
    LocalTime startTime,
    LocalTime endTime,
    int arrprevstationcnt,
    int arrtime,
    String nodeid,
    String nodenm,
    String routeid,
    String routeno,
    String routetp,
    String vehicletp
) {

}
