package com.example.busnotice.domain.bus.res;

import com.example.busnotice.domain.schedule.res.ScheduleResponse;
import java.time.LocalTime;
import java.util.List;
import lombok.Data;

@Data
public class BusStationArriveResponse {

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

        // Item 객체를 NowScheduleResponse로 변환
        public ScheduleResponse toResponseDto(String days, LocalTime startTime, LocalTime endTime) {
            return new ScheduleResponse(
                days,
                startTime,
                endTime,
                this.arrprevstationcnt,
                this.arrtime,
                this.nodeid,
                this.nodenm,
                this.routeid,
                this.routeno,
                this.routetp,
                this.vehicletp
            );
        }
    }
}
