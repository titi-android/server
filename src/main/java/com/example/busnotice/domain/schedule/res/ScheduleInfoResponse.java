package com.example.busnotice.domain.schedule.res;

import com.example.busnotice.domain.bus.Bus;
import com.example.busnotice.domain.schedule.Schedule;
import java.time.LocalTime;
import java.util.List;

public record ScheduleInfoResponse(
    Long id,
    String name,
    String days,
    LocalTime startTime,
    LocalTime endTime,
    String busStopName,
    List<String> busNames
) {

    public static ScheduleInfoResponse fromEntity(Schedule s) {
        return new ScheduleInfoResponse(
            s.getId(), s.getName(), s.getDays(), s.getStartTime(), s.getEndTime(),
            s.getBusStop().getName(),
            s.getBusStop().getBusList().stream().map(Bus::getName).toList()
        );
    }
}
