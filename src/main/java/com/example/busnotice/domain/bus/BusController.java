package com.example.busnotice.domain.bus;

import com.example.busnotice.domain.bus.res.BusArrInfosDto.Item;
import com.example.busnotice.domain.bus.res.BusNameAndTypeResponse;
import com.example.busnotice.domain.busStop.CityCodeRepository;
import com.example.busnotice.global.code.StatusCode;
import com.example.busnotice.global.format.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @GetMapping("/nodes/arrive/info")
    @Operation(
        summary = "특정 노드에 도착 예정인 모든 버스 정보 조회",
        description = "도착 예정인 버스가 없는 경우 빈 리스트를 반환"
    )
    public ApiResponse<List<Item>> getNodeArriveInfo(
        @RequestParam("cityName") String cityName, // 도시 코드
        @RequestParam("nodeId") String nodeId // 노드 ID
    ) throws UnsupportedEncodingException {
        List<Item> items = busService.특정_노드_ID에_도착하는_모든_버스들_정보_조회(cityName, nodeId);
        String msg = items.isEmpty()
            ? "현재 도착 예정인 버스가 존재하지 않습니다."
            : "현재 도착 예정인 버스가 존재합니다.";
        return ApiResponse.createSuccessWithData(items, msg);
    }

    @Operation(summary = "특정 노드에 도착 예정인 특정 버스들의 가장 빠른 정보 조회")
//    @GetMapping("/nodes/arrive/info/specific")
    public Item getNodeSpecificArriveInfo(
        @RequestParam("cityCode") String cityCode,
        @RequestParam("nodeId") String nodeId,
        @RequestParam("routeNo") List<String> busList // 버스 번호들 ex) 급행6, 410-1 등
    ) throws UnsupportedEncodingException {
        Item item = busService.특정_노드_ID에_가장_빨리_도착하는_버스_조회(cityCode, nodeId, busList);
        return item;
    }

    @Operation(summary = "특정 노드를 경유하는 모든 버스들 이름과 종류 조회")
    @GetMapping("/nodes/bus-names/all")
    public ApiResponse<List<BusNameAndTypeResponse>> getBusNamesOfNode(
        @RequestParam("cityName") String cityName,
        @RequestParam("nodeId") String nodeId
    ) throws UnsupportedEncodingException {
        List<BusNameAndTypeResponse> busNameAndTypeList = busService.특정_노드_ID를_경유하는_모든_버스들_이름_조회(cityName, nodeId);
        return ApiResponse.createSuccessWithData(busNameAndTypeList);
    }

    @Operation(summary = "특정 노드를 경유하는 버스들이 맞는지 확인")
    @GetMapping("/nodes/bus-names/check")
    public ApiResponse<String> getBusNamesOfNode(
        @RequestParam("cityName") String cityName,
        @RequestParam("nodeId") String nodeId,
        @RequestParam("routeNo") List<String> busList
    ) throws UnsupportedEncodingException {
        List<BusNameAndTypeResponse> busNameAndTypeResponses = busService.특정_노드_ID를_경유하는_모든_버스들_이름_조회(cityName, nodeId);
        List<String> busNames = busNameAndTypeResponses.stream().map(bnt -> bnt.name()).toList();
        boolean isValid = busNames.containsAll(busList.stream().map(String::trim).toList());
        return isValid
            ? ApiResponse.createSuccess("올바른 버스 목록입니다.")
            : ApiResponse.createSuccess("해당 정류장에 속한 버스 노선이 아닙니다.");
    }
}
