package com.example.busnotice.domain.busStop.res;

import com.example.busnotice.util.ItemsDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import lombok.Data;

@Data
public class BusStopsDto {

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
    public static class Items {

        @JsonDeserialize(using = ItemsDeserializer.class)
        private List<Item> item;
    }

    @Data
    public static class Item {

        private double gpslati;
        private double gpslong;
        private String nodeid;
        private String nodenm;
        private String nodeno;
    }
}