package com.example.busnotice.domain.schedule.res;

public record NowScheduleResponse(
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
