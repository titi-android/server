package com.example.busnotice.domain.schedule.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.util.List;

public record ScheduleResponses(
    Long id,
    String name,
    List<String> daysList,
    @JsonFormat(pattern = "HH:mm")
    LocalTime startTime,
    @JsonFormat(pattern = "HH:mm")
    LocalTime endTime,
    String busStopName,
    List<BusInfoDto> busInfos,
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
