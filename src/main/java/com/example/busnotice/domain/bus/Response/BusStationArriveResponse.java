package com.example.busnotice.domain.bus.Response;

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
    }
}
