package com.example.busnotice.domain.schedule.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.util.List;

public record ScheduleResponse(
    Long id, // 스케줄 id
    String name, // 스케줄 이름
    List<String> days, // 요일 목록
    @Schema(type = "string", example = "00:00")
    @JsonFormat(pattern = "HH:mm")
    LocalTime startTime,
    @Schema(type = "string", example = "00:00")
    @JsonFormat(pattern = "HH:mm")
    LocalTime endTime,
    List<BusStopArrInfoDto> busStopInfos,
    String desBusStopName,
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
