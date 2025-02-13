package com.example.busnotice.domain.schedule.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.util.List;

public record UpdateScheduleRequest(
    String name, // 스케줄 이름
    List<String> daysList,  // 요일 리스트
    @Schema(type = "string", example = "00:00")
    @JsonFormat(pattern = "HH:mm")
    LocalTime startTime, // 시작 시간
    @Schema(type = "string", example = "00:00")
    @JsonFormat(pattern = "HH:mm")
    LocalTime endTime, // 마치는 시간
    List<RouteInfo> routeInfos,
    DestinationInfo destinationInfo,
    Boolean isAlarmOn // 잠금화면 알림 여부
) {

    public record RouteInfo(
        String regionName,
        String busStopName,
        String nodeId,
        List<CreateScheduleRequest.RouteInfo.BusInfo> busInfos
    ) {

        public record BusInfo(
            String name,
            String type
        ) {

        }
    }

    public record DestinationInfo(
        String regionName,
        String busStopName,
        String nodeId

    ) {

    }
}
