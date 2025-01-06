package com.example.busnotice.domain.bus.res;

import lombok.Data;
@Data
public class BusStationResponse {

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
        private Items items; // 변경: List<Item>에서 Object로 변경
        private int numOfRows;
        private int pageNo;
        private int totalCount;
    }

    @Data
    public static class Item {
        private double gpslati;
        private double gpslong;
        private String nodeid;
        private String nodenm;
        private String nodeno;
    }
    @Data
    public static class Items {
        private Item item;
    }
}