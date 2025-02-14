package com.example.busnotice.domain.schedule.res;

import com.example.busnotice.domain.bus.Bus;
import com.example.busnotice.domain.schedule.Schedule;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.util.List;

public record ScheduleInfoResponse(
    Long id,
    String name,
    List<String> days,
    @JsonFormat(pattern = "HH:mm")
    LocalTime startTime,
    @JsonFormat(pattern = "HH:mm")
    LocalTime endTime,
    String regionName,
    String busStopName,
    String nodeId,
    List<BusInfo> busInfos,
    boolean isAlarmOn
) {

    public static ScheduleInfoResponse fromEntity(Schedule s) {
        List<Bus> busList = s.getBusStop().getBusList();
        List<BusInfo> busInfos = busList.stream().map(b -> new BusInfo(b.getName(), b.getType()))
            .toList();
        return new ScheduleInfoResponse(
            s.getId(), s.getName(), s.getDaysList(), s.getStartTime(), s.getEndTime(),
            s.getRegionName(),
            s.getBusStop().getName(), s.getBusStop().getNodeId(),
            busInfos, s.getIsAlarmOn()
        );
    }

    public record BusInfo(
        String name,
        String type
    ) {

    }
}
