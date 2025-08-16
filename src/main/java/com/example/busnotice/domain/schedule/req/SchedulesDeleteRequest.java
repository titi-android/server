package com.example.busnotice.domain.schedule.req;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "여러 스케줄 삭제 요청 DTO")
public record SchedulesDeleteRequest(
        @Schema(description = "삭제할 스케줄 ID 리스트", example = "[1, 2, 3]")
        List<Long> scheduleIds
) {
}


