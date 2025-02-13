package com.example.busnotice.domain.schedule.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.util.List;

public record ScheduleResponse(
    Long id, // 스케줄 id
    String name, // 스케줄 이름
    List<String> days, // 요일 목록
    @Schema(type = "array", example = "[0,0]")
    LocalTime startTime,
    @Schema(type = "array", example = "[0,0]")
    LocalTime endTime,
    List<BusStopArrInfoDto> busStopInfos,
    Boolean isAlarmOn
) {

    public record BusStopArrInfoDto(
        String busStopName,
        List<BusArrInfoDto> busInfos
    ) {

        public record BusArrInfoDto(
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
}
