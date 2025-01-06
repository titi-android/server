package com.example.busnotice.domain.schedule.res;

import java.util.List;

// 해당 스케줄의 버스들 중 가장 빠른 시간대의 버스
public record ScheduleResponse(
    List<ScheduleResponseDTO> buses
) {
    record ScheduleResponseDTO(
        String busName,
        Long arrTime
    ) {

    }
}
