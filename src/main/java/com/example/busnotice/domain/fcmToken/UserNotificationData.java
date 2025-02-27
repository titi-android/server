package com.example.busnotice.domain.fcmToken;

import com.example.busnotice.domain.schedule.res.ScheduleResponse;
import com.example.busnotice.domain.schedule.res.ScheduleResponse.BusStopArrInfoDto;
import com.example.busnotice.domain.schedule.res.ScheduleResponse.BusStopArrInfoDto.BusArrInfoDto;
import java.util.List;

public record UserNotificationData(
    String token,
    String scheduleName,
    List<String> days,
    List<BusStopArrInfoDto> busStopInfos
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
