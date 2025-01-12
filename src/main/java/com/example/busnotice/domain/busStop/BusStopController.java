package com.example.busnotice.domain.busStop;

import com.example.busnotice.global.format.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "BusStop", description = "버스정류장 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BusStopController {

    private final BusStopService busStopService;

    @GetMapping("/cityCode")
    @Operation(summary = "도시 이름으로 도시 코드 조회")
    public ApiResponse<String> getCityCode(@RequestParam("cityName") String cityName)
        throws UnsupportedEncodingException {
        String cityCodes = busStopService.도시코드_조회(cityName);
        return ApiResponse.createSuccessWithData(cityCodes, "도시코드 조회에 성공했습니다.");
    }

    @GetMapping("/node/id")
    @Operation(summary = "정류소의 노드 ID 조회")
    public ApiResponse<String> getNodeId
        (
            @RequestParam("cityName") String cityCode, // 도시 코드
            @RequestParam("name") String name // 정류소 이름
        ) throws IOException {

        String nodeId = busStopService.버스정류장_노드_ID_조회(cityCode, name);
        return ApiResponse.createSuccessWithData(nodeId, "버스정류장 노드 ID 조회에 성공했습니다.");
    }
}
