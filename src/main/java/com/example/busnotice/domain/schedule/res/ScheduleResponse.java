package com.example.busnotice.domain.schedule.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.util.List;

public record ScheduleResponse(
    Long id,
    String name,
    List<String> days,

    @Schema(type = "array", example = "[0,0]")
    LocalTime startTime,

    @Schema(type = "array", example = "[0,0]")
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
