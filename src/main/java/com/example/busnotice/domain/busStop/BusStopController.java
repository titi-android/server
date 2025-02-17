package com.example.busnotice.domain.busStop;

import com.example.busnotice.domain.bus.res.BusInfosResponse;
import com.example.busnotice.global.format.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "BusStop", description = "버스정류장 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BusStopController {

    private final BusStopService busStopService;

    @GetMapping("/cityCode")
    @Operation(summary = "도시 이름으로 도시 코드 조회")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "CITY_CODE401", description = "해당 이름과 매칭되는 도시코드가 존재하지 않습니다."),
    })
    public ApiResponse<String> getCityCode(@RequestParam("cityName") String cityName) {
        String cityCodes = busStopService.도시코드_DB_조회(cityName);
        return ApiResponse.createSuccessWithData(cityCodes, "도시코드 조회에 성공했습니다.");
    }

    @GetMapping("/nodes/id")
    @Operation(summary = "정류소의 노드 ID 조회")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "BUS_STOP401", description = "해당 이름을 포함하는 버스정류장이 존재하지 않습니다."),
    })
    public ApiResponse<String> getNodeId
        (
            @RequestParam("cityName") String cityName, // 도시 이름
            @RequestParam("busStopName") String busStopName // 정류소 이름
        ) throws IOException {

        String nodeId = busStopService.버스정류장_노드_ID_조회(cityName, busStopName);
        return ApiResponse.createSuccessWithData(nodeId, "버스정류장 노드 ID 조회에 성공했습니다.");
    }

    @GetMapping("/nodes/names")
    @Operation(summary = "버스정류장 목록(이름만) 조회")
    public ApiResponse<List<String>> getNodeNames
        (
            @RequestParam("cityName") String cityName, // 도시 이름
            @RequestParam("busStopName") String busStopName // 정류소 이름
        ) throws IOException {
        List<String> busNames = busStopService.해당_이름을_포함하는_버스정류장_목록_조회_이름만_반환(cityName,
            busStopName);
        String msg =
            busNames.isEmpty() ? "해당 이름을 포함하는 버스정류장이 존재하지 않습니다" : "해당 이름을 포함하는 버스정류장이 존재합니다.";
        return ApiResponse.createSuccessWithData(busNames, msg);
    }

    @GetMapping("/nodes/infos")
    @Operation(summary = "버스정류장 목록(모든 정보) 조회")
    public Mono<ApiResponse<BusInfosResponse>> getNodeInfos
        (
            @RequestParam("cityName") String cityName, // 도시 이름
            @RequestParam("busStopName") String busStopName // 정류소 이름
        ) throws IOException {
        Mono<BusInfosResponse> monoBusInfoResponse = busStopService.해당_이름을_포함하는_버스정류장_목록_조회_모든_정보_반환(
            cityName, busStopName);

        return monoBusInfoResponse.map(busInfosResponse -> {
            String msg = busInfosResponse.busInfoResponses().isEmpty() ?
                "해당 이름을 포함하는 버스정류장이 존재하지 않습니다" :
                "해당 이름을 포함하는 버스정류장이 존재합니다.";

            return ApiResponse.createSuccessWithData(busInfosResponse, msg);
        });
    }
}
