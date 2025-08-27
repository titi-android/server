package com.example.busnotice.domain.schedule.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.List;

public record UpdateScheduleRequest(
        String name,
        List<String> daysList,
        @Schema(type = "string", example = "00:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,
        @Schema(type = "string", example = "00:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,
        Boolean isAlarmOn,
        DestinationInfo destinationInfo,
        List<RouteInfo> routeInfos
) {
    public record DestinationInfo(
            String type,
            String regionName,// "BUS" 또는 "SUBWAY"
            String desName,
            String lineName,
            String dirName,
            String dir
    ) {
    }

    public record RouteInfo(
            String type, // "BUS" or "SUBWAY"
            BusStopSectionInfo busStopSection, // BUS일 때만 사용
            SubwaySectionInfo subwaySection    // SUBWAY일 때만 사용
    ) {
    }

    public record BusStopSectionInfo(
            String regionName,
            String busStopName,
            String nodeId,
            List<BusInfo> busList
    ) {
    }

    public record BusInfo(
            String name,
            String type
    ) {
    }

    public record SubwaySectionInfo(
            String regionName,
            String lineName,
            String stationName,
            String dirName, // 방면 정보
            String dir // "UP" (상행), "DOWN" (하행) 등
    ) {
    }
}