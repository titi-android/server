package com.example.busnotice.domain.schedule.res;

import java.time.LocalTime;
import java.util.List;

public record ScheduleArrivalResponse(
        Long id,
        String name,
        List<String> days,
        LocalTime startTime,
        LocalTime endTime,
        List<SectionArrInfoDto> sections, // 순서대로 환승 구간 정보
        String destinationName,
        Boolean isAlarmOn
) {
    public record SectionArrInfoDto(
            String type, // "BUS" or "SUBWAY"
            BusStopArrInfo busStop,         // BUS일 때만 값 존재, 아니면 null
            SubwayArrInfo subway,           // SUBWAY일 때만 값 존재, 아니면 null
            int orderIndex                  // 환승 순서
    ) {
        public record BusStopArrInfo(
                String busStopName,
                List<BusArrInfo> busArrivals // 도착 예정 버스 2개 등
        ) {
            public record BusArrInfo(
                    int arrprevstationcnt,
                    int arrtime,
                    String nodeid,
                    String nodenm,
                    String routeid,
                    String routeno,
                    String routetp,
                    String vehicletp
            ) {}
        }

        public record SubwayArrInfo(
                String regionName,
                String lineName,
                String stationName,
                String dir,
                List<SubwayArrival> arrivals // 도착 예정 열차 2개 등
        ) {
            public record SubwayArrival(
                    String subwayId,
                    String updnLine,
                    String statnNm,
                    String barvlDt,
                    String arvlMsg2,
                    String arvlCd
            ) {}
        }
    }
}
