package com.example.busnotice.domain.bus;

import com.example.busnotice.domain.bus.res.BusStationArriveResponse.Item;
import java.io.UnsupportedEncodingException;
import java.util.List;
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
            @RequestParam String cityCode, // 도시 코드
            @RequestParam String name // 정류소 이름
        ) throws UnsupportedEncodingException {

        String nodeId = busService.버스정류장_노드_ID_조회(cityCode, name);
        return nodeId;
    }

    @Description("특정 노드에 도착 예정인 모든 버스 정보 조회")
    @GetMapping("/node/arrive/info")
    List<Item> getNodeArriveInfo(
        @RequestParam String cityCode, // 도시 코드
        @RequestParam String nodeId // 노드 ID
    ) throws UnsupportedEncodingException {
        List<Item> items = busService.특정_노드_ID에_도착하는_모든_버스들_정보_조회(cityCode, nodeId);
        return items;
    }

    @Description("특정 노드에 도착 예정인 특정 버스들의 가장 빠른 정보 조회")
    @GetMapping("/node/arrive/info/specific")
    Item getNodeSpecificArriveInfo(
        @RequestParam String cityCode,
        @RequestParam String nodeId,
        @RequestParam("routeNo") List<String> busList // 버스 번호들 ex) 급행6, 410-1 등
    ) throws UnsupportedEncodingException {
        Item item = busService.특정_노드_ID에_가장_빨리_도착하는_버스_조회(cityCode, nodeId, busList);
        return item;
    }
}
