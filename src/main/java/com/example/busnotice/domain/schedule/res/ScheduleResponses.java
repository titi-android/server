package com.example.busnotice.domain.schedule.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.util.List;

public record ScheduleResponses(
    Long id,
    String name,
    String days,

    @Schema(type = "array", example = "[0,0]")
    LocalTime startTime,

    @Schema(type = "array", example = "[0,0]")
    LocalTime endTime,

    String busStopName,

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
