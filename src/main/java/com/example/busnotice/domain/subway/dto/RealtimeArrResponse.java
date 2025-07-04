package com.example.busnotice.domain.subway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RealtimeArrResponse {
    @JsonProperty("errorMessage")
    private ErrorMessage errorMessage;
    @JsonProperty("realtimeArrivalList")
    private List<RealtimeArrival> realtimeArrivalList;

    @Data
    public static class ErrorMessage {
        private int status;
        private String code;
        private String message;
        private String link;
        private String developerMessage;
        private int total;
    }

    @Data
    public static class RealtimeArrival {
        private String subwayId;
        private String updnLine;
        private String statnNm;
        private String barvlDt;
        private String arvlMsg2;
        private String arvlCd;
        // ... 필요한 필드 추가
    }
}