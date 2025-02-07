package com.example.busnotice.domain.schedule.res;

import com.example.busnotice.domain.bus.Bus;
import com.example.busnotice.domain.schedule.Schedule;
import java.time.LocalTime;
import java.util.List;

public record ScheduleInfoResponse(
    Long id,
    String name,
    List<String> days,
    LocalTime startTime,
    LocalTime endTime,
    String regionName,
    String busStopName,
    List<String> busNames,
    boolean isAlarmOn
) {

    public static ScheduleInfoResponse fromEntity(Schedule s) {
        return new ScheduleInfoResponse(
            s.getId(), s.getName(), s.getDaysList(), s.getStartTime(), s.getEndTime(), s.getRegionName(),
            s.getBusStop().getName(),
            s.getBusStop().getBusList().stream().map(Bus::getName).toList(), s.getIsAlarmOn()
        );
    }
}
