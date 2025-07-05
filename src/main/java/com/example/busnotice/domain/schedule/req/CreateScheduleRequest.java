package com.example.busnotice.domain.schedule.req;

import java.time.LocalTime;
import java.util.List;

public record CreateScheduleRequest(
        String name,
        List<String> daysList,
        LocalTime startTime,
        LocalTime endTime,
        Boolean isAlarmOn,
        DestinationInfo destinationInfo,
        List<RouteInfo> routeInfos
) {
    public record DestinationInfo(
            String type,      // "BUS" 또는 "SUBWAY"
            String regionName,
            String placeName,
            String nodeId
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
            String dir // "UP" (상행), "DOWN" (하행) 등
    ) {
    }
}

