package com.example.busnotice.domain.schedule.res;

import java.time.LocalTime;
import java.util.List;

public record ScheduleResponses(

    String name,
    String days,

    LocalTime startTime,
    LocalTime endTime,

    List<BusInfoDto> busInfos
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
