//package com.example.busnotice.domain.schedule.res;
//
//import com.example.busnotice.domain.busStop.BusStop;
//import com.example.busnotice.domain.schedule.Schedule;
//import com.fasterxml.jackson.annotation.JsonFormat;
//import io.swagger.v3.oas.annotations.media.Schema;
//
//import java.time.LocalTime;
//import java.util.List;
//
//public record ScheduleInfoResponse(
//        Long id,
//        String name,
//        List<String> days,
//        @Schema(type = "string", example = "00:00")
//        @JsonFormat(pattern = "HH:mm")
//        LocalTime startTime,
//        @Schema(type = "string", example = "00:00")
//        @JsonFormat(pattern = "HH:mm")
//        LocalTime endTime,
//        List<BusStopInfo> busStops,
//        DestinationInfo destinationInfo,
//        boolean isAlarmOn
//) {
//
//
//    public static ScheduleInfoResponse fromEntity(Schedule s) {
//        List<BusStop> busStops = s.getBusStops();
//        List<BusStopInfo> busStopInfos = busStops.stream().map(busStop -> {
//            List<BusInfo> busInfos = busStop.getBusList().stream()
//                    .map(bus -> new BusInfo(bus.getName(), bus.getType())).toList();
//            return new BusStopInfo(busStop.getCityCode(), busStop.getRegionName(),
//                    busStop.getName(),
//                    busStop.getNodeId(), busInfos);
//        }).toList();
//        DestinationInfo destinationInfo = new DestinationInfo(s.getDestinationInfo().regionName,
//                s.getDestinationInfo().busStopName, s.getDestinationInfo().nodeId);
//        return new ScheduleInfoResponse(
//                s.getId(), s.getName(), s.getDaysList(), s.getStartTime(), s.getEndTime(),
//                busStopInfos,
//                destinationInfo, s.getIsAlarmOn()
//        );
//    }
//
//    public record BusStopInfo(
//            String cityCode,
//            String regionName,
//            String busStopName,
//            String nodeId,
//            List<BusInfo> busInfos
//    ) {
//
//    }
//
//    public record BusInfo(
//            String name,
//            String type
//    ) {
//
//    }
//
//    public record DestinationInfo(
//            String regionName,
//            String busStopName,
//            String nodeId
//    ) {
//
//    }
//}
