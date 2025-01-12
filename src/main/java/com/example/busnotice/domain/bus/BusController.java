package com.example.busnotice.domain.bus;

import com.example.busnotice.domain.bus.res.BusStationArriveResponse.Item;
import com.example.busnotice.global.code.StatusCode;
import com.example.busnotice.global.format.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.UnsupportedEncodingException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Bus", description = "버스 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BusController {

    private final BusService busService;

    @Operation(summary = "특정 노드에 도착 예정인 모든 버스 정보 조회")
    @GetMapping("/node/arrive/info")
    public List<Item> getNodeArriveInfo(
        @RequestParam("cityCode") String cityCode, // 도시 코드
        @RequestParam("nodeId") String nodeId // 노드 ID
    ) throws UnsupportedEncodingException {
        List<Item> items = busService.특정_노드_ID에_도착하는_모든_버스들_정보_조회(cityCode, nodeId);
        return items;
    }

    @Operation(summary = "특정 노드에 도착 예정인 특정 버스들의 가장 빠른 정보 조회")
//    @GetMapping("/node/arrive/info/specific")
    public Item getNodeSpecificArriveInfo(
        @RequestParam("cityCode") String cityCode,
        @RequestParam("nodeId") String nodeId,
        @RequestParam("routeNo") List<String> busList // 버스 번호들 ex) 급행6, 410-1 등
    ) throws UnsupportedEncodingException {
        Item item = busService.특정_노드_ID에_가장_빨리_도착하는_버스_조회(cityCode, nodeId, busList);
        return item;
    }

    @Operation(summary = "특정 노드를 경유하는 모든 버스들 이름 조회")
//    @GetMapping("/node/bus-names/all")
    public List<String> getBusNamesOfNode(
        @RequestParam("cityCode") String cityCode,
        @RequestParam("nodeId") String nodeId
    ) throws UnsupportedEncodingException {
        List<String> busNames = busService.특정_노드_ID를_경유하는_모든_버스들_이름_조회(cityCode, nodeId);
        return busNames;
    }

    @Operation(summary = "특정 노드를 경유하는 버스들이 맞는지 확인")
    @GetMapping("/node/bus-names/check")
    public ApiResponse<String> getBusNamesOfNode(
        @RequestParam("cityCode") String cityCode,
        @RequestParam("nodeId") String nodeId,
        @RequestParam("routeNo") List<String> busList
    ) throws UnsupportedEncodingException {
        List<String> allBusNames = busService.특정_노드_ID를_경유하는_모든_버스들_이름_조회(cityCode, nodeId);
        boolean isValid = allBusNames.containsAll(busList.stream().map(String::trim).toList());
        return isValid
            ? ApiResponse.createSuccess("올바른 버스 목록입니다.")
            : ApiResponse.createFail(StatusCode.BAD_REQUEST, "해당 정류장에 속한 버스 노선이 아닙니다.");
    }
}
