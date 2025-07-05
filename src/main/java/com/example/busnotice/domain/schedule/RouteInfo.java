package com.example.busnotice.domain.schedule;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@Embeddable
@AllArgsConstructor
public class RouteInfo {

    private String regionName;
    private String busStopName;
    private String nodeId;
    private List<BusInfo> busInfos;

    public RouteInfo() {

    }
}
