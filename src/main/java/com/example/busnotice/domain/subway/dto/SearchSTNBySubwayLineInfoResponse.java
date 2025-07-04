package com.example.busnotice.domain.subway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SearchSTNBySubwayLineInfoResponse {
    @JsonProperty("SearchSTNBySubwayLineInfo")
    private SearchSTNBySubwayLineInfo searchSTNBySubwayLineInfo;

    @Data
    public static class SearchSTNBySubwayLineInfo {
        @JsonProperty("list_total_count")
        private int listTotalCount;

        @JsonProperty("RESULT")
        private Result result;

        @JsonProperty("row")
        private List<Row> row;

        @Data
        public static class Result {
            @JsonProperty("CODE")
            private String code;

            @JsonProperty("MESSAGE")
            private String message;
        }

        @Data
        public static class Row {
            @JsonProperty("STATION_CD")
            private String stationCd;

            @JsonProperty("STATION_NM")
            private String stationNm;

            @JsonProperty("STATION_NM_ENG")
            private String stationNmEng;

            @JsonProperty("LINE_NUM")
            private String lineNum;

            @JsonProperty("FR_CODE")
            private String frCode;

            @JsonProperty("STATION_NM_CHN")
            private String stationNmChn;

            @JsonProperty("STATION_NM_JPN")
            private String stationNmJpn;
        }
    }
}
