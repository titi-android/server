package com.example.busnotice.domain.schedule;

import jakarta.persistence.Embeddable;
import java.util.List;
import lombok.Data;

@Data
@Embeddable
public class RouteInfo {

    private String regionName;
    private String busStopName;
    private String nodeId;
    private List<BusInfo> busInfos;

    public RouteInfo(String regionName, String busStopName, String nodeId, List<BusInfo> busInfos) {
        this.regionName = regionName;
        this.busStopName = busStopName;
        this.nodeId = nodeId;
        this.busInfos = busInfos;
    }

    public RouteInfo() {

    }
}
