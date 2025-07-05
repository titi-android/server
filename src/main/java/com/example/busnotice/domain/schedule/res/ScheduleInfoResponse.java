package com.example.busnotice.domain.schedule.res;

import java.util.List;

public record ScheduleInfoResponse(
        Long scheduleId,
        String name,
        List<String> daysList,
        String startTime,
        String endTime,
        Boolean isAlarmOn,
        DestinationInfo destinationInfo,
        List<RouteInfo> routeInfos
) {
    public record DestinationInfo(
            String type,      // "BUS" 또는 "SUBWAY"
            String desName // 목적지 이름
    ) {
    }

    public record RouteInfo(
            String type, // "BUS" or "SUBWAY"
            BusStopSectionInfo busStopSection, // BUS일 때만
            SubwaySectionInfo subwaySection    // SUBWAY일 때만
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
