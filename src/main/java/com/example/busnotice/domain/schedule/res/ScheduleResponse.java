package com.example.busnotice.domain.schedule.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.util.List;

public record ScheduleResponse(
    Long id,
    String name,
    List<String> days,
    @JsonFormat(pattern = "HH:mm")
    LocalTime startTime,
    @JsonFormat(pattern = "HH:mm")
    LocalTime endTime,
    BusInfoDto busInfo,
    Boolean isAlarmOn
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
