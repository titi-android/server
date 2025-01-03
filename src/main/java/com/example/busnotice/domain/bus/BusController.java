package com.example.busnotice.domain.bus;

import com.example.busnotice.domain.bus.Response.BusStationArriveResponse;
import com.example.busnotice.domain.bus.Response.BusStationArriveResponse.Item;
import com.example.busnotice.domain.bus.Response.BusStationResponse;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BusController {

    private final BusService busService;

    @Description("정류소의 노드 ID 조회")
    @GetMapping("/node/id")
    String getNodeId
        (
            @RequestParam Long cityCode, // 도시 코드
            @RequestParam String name // 정류소 이름
        ) throws UnsupportedEncodingException {

        BusStationResponse result = busService.getBusStationInfo(cityCode, name);
        return result.getResponse().getBody().getItems().getItem().getNodeid();
    }

    @Description("특정 노드에 도착 예정인 모든 버스 정보 조회")
    @GetMapping("/node/arrive/info")
    BusStationArriveResponse getNodeArriveInfo(
        @RequestParam Long cityCode, // 도시 코드
        @RequestParam String nodeId // 노드 ID
    ) throws UnsupportedEncodingException {
        return busService.getBusStationArriveInfo(cityCode, nodeId);
    }

    @Description("특정 노드에 도착 예정인 특정 버스들의 가장 빠른 정보 조회")
    @GetMapping("/node/arrive/info/specific")
    List<Item> getNodeSpecificArriveInfo(
        @RequestParam Long cityCode,
        @RequestParam String nodeId,
        @RequestParam("routeNo") String[] routeNos // 버스 번호들 ex) 급행6, 410-1 등
    ) throws UnsupportedEncodingException {
        BusStationArriveResponse result = busService.getBusStationArriveInfo(cityCode, nodeId);
        List<Item> items = busService.filterArriveInfo(result, routeNos);

        List<Item> minArrTimeItems = busService.getMinArrTimeItems(items);
        return minArrTimeItems;
    }
}
