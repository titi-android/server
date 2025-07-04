package com.example.busnotice.domain.bus.res;

import com.example.busnotice.domain.schedule.res.ScheduleResponse.BusStopArrInfoDto.BusArrInfoDto;
import com.example.busnotice.util.BusArrInfosDtoItemsDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;

@Data
public class BusArrInfosDto {

    private Response response;

    @Data
    public static class Response {

        private Header header;
        private Body body;
    }

    @Data
    public static class Header {

        private String resultCode;
        private String resultMsg;
    }

    @Data
    public static class Body {

        private Items items;
        private int numOfRows;
        private int pageNo;
        private int totalCount;
    }

    @Data
    public static class Items {

        @JsonDeserialize(using = BusArrInfosDtoItemsDeserializer.class)
        private List<Item> item;
    }

    @Data
    public static class Item {

        private int arrprevstationcnt;
        private int arrtime;
        private String nodeid;
        private String nodenm;
        private String routeid;
        private String routeno;
        private String routetp;
        private String vehicletp;

        public Item() {

        }

        public Item(int arrprevstationcnt, int arrtime, String nodeId, String nodenm,
                    String routeid, String routeno, String routetp, String vehicletp) {
            this.arrprevstationcnt = arrprevstationcnt;
            this.arrtime = arrtime;
            this.nodeid = nodeId;
            this.nodenm = nodenm;
            this.routeid = routeid;
            this.routeno = routeno;
            this.routetp = routetp;
            this.vehicletp = vehicletp;
        }

        // Item 객체를 NowScheduleResponse로 변환
//        public ScheduleResponse toScheduleResponse(Long id, String name, List<String> daysList,
//            LocalTime startTime,
//            LocalTime endTime, Boolean isAlarmOn) {
//            return new ScheduleResponse(
//                id,
//                name,
//                daysList,
//                startTime,
//                endTime,
//                new ScheduleResponse.BusInfoDto(this.arrprevstationcnt,
//                    this.arrtime,
//                    this.nodeid,
//                    this.nodenm,
//                    this.routeid,
//                    this.routeno,
//                    this.routetp,
//                    this.vehicletp),
//                isAlarmOn
//            );
//        }

        public BusArrInfoDto toBusInfoDto(int arrprevstationcnt, int arrtime, String nodeid,
                                          String nodenm, String routeid, String routeno, String routetp, String vehicletp) {
            return new BusArrInfoDto(
                    arrprevstationcnt,
                    arrtime,
                    nodeid,
                    nodenm,
                    routeid,
                    routeno,
                    routetp,
                    vehicletp
            );
        }
    }
}
