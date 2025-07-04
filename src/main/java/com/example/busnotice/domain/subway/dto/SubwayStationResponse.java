package com.example.busnotice.domain.subway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SubwayStationResponse(

        @JsonProperty("outStnNum")
        String stationId,

        @JsonProperty("stnKrNm")
        String stationName,

        @JsonProperty("lineNm")
        String lineName,

        @JsonProperty("convX")
        String longitude,

        @JsonProperty("convY")
        String latitude

) {
}
