package com.example.busnotice.domain.bus.res;

import com.example.busnotice.util.BusInfosDtoItemsDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class BusInfosDto {

    @JsonProperty("response")
    private Response response;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {

        @JsonProperty("header")
        private Header header;

        @JsonProperty("body")
        private Body body;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {

        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMsg")
        private String resultMsg;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {

        @JsonProperty("items")
        private Items items;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {

        @JsonDeserialize(using = BusInfosDtoItemsDeserializer.class)
        private List<Item> item;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        public Item() {
            // 기본 생성자
        }

        @JsonProperty("endnodenm")
        private String endNodeNm;

        @JsonProperty("routeid")
        private String routeId;

        @JsonProperty("routeno")
        private String routeNo;

        @JsonProperty("routetp")
        private String routeTp;

        @JsonProperty("startnodenm")
        private String startNodeNm;
    }
}
