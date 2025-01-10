package com.example.busnotice.domain.schedule.res;

import java.time.LocalTime;

public record ScheduleResponse(
    String name,
    String days,
    LocalTime startTime,
    LocalTime endTime,
    BusInfoDto busInfo
) {

    public record BusInfoDto(
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
}
