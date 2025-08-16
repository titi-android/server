package com.example.busnotice.domain.fcmToken;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;

public record UserNotificationData(
        String token,
        Long scheduleId,
        String scheduleName,
        List<String> days,
        List<SectionInfoDto> sections // BUS, SUBWAY 상속 구조
) {
    // 부모 DTO (공통 필드)
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = BusSectionInfoDto.class, name = "BUS"),
            @JsonSubTypes.Type(value = SubwaySectionInfoDto.class, name = "SUBWAY")
    })
    public static abstract class SectionInfoDto {
        public abstract String type();
    }

    // 버스 전용
    public static class BusSectionInfoDto extends SectionInfoDto {
        private final String busStopName;
        private final List<BusArrInfoDto> busArrivals;

        public BusSectionInfoDto(String busStopName, List<BusArrInfoDto> busArrivals) {
            this.busStopName = busStopName;
            this.busArrivals = busArrivals;
        }

        @Override
        public String type() {
            return "BUS";
        }

        public String busStopName() {
            return busStopName;
        }

        public List<BusArrInfoDto> busArrivals() {
            return busArrivals;
        }
    }

    // 지하철 전용
    public static class SubwaySectionInfoDto extends SectionInfoDto {
        private final String stationName;
        private final List<SubwayArrInfoDto> subwayArrivals;

        public SubwaySectionInfoDto(String stationName, List<SubwayArrInfoDto> subwayArrivals) {
            this.stationName = stationName;
            this.subwayArrivals = subwayArrivals;
        }

        @Override
        public String type() {
            return "SUBWAY";
        }

        public String stationName() {
            return stationName;
        }

        public List<SubwayArrInfoDto> subwayArrivals() {
            return subwayArrivals;
        }
    }

    // 버스 도착 정보
    public record BusArrInfoDto(
            int arrprevstationcnt,
            int arrtime,
            String nodeid,
            String nodenm,
            String routeid,
            String routeno,
            String routetp,
            String vehicletp
    ) {}

    // 지하철 도착 정보
    public record SubwayArrInfoDto(
            String subwayId,
            String updnLine,
            String statnNm,
            String barvlDt,
            String arvlMsg2,
            String arvlCd
    ) {}
}