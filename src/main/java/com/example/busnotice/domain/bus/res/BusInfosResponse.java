package com.example.busnotice.domain.bus.res;

import java.util.List;

public record BusInfosResponse(

    List<BusInfoResponse> busInfoResponses

) {

    public record BusInfoResponse(
        String name,
        String nodeId,
        double tmX,
        double tmY
    ) {

    }
}
