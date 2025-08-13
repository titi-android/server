package com.example.busnotice.domain.subway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SubwayStationResponse(

        // 외구간_역_수
        @JsonProperty("outStnNum")
        String stationId,

        // 역_한글_명칭
        @JsonProperty("stnKrNm")
        String stationName,

        // 호선_명칭
        @JsonProperty("lineNm")
        String lineName,

        // 환승역_X
        @JsonProperty("convX")
        String longitude,

        // 환승역_Y
        @JsonProperty("convY")
        String latitude

) {
}
