package com.example.busnotice.domain.busStop;

import com.example.busnotice.global.format.ApiResponse;
import java.io.UnsupportedEncodingException;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BusStopController {

    private final BusStopService busStopService;

    @GetMapping("/cityCode")
    @Description("도시 이름으로 도시 코드 조회")
    public ApiResponse<String> getCityCode(@RequestParam String cityName)
        throws UnsupportedEncodingException {
        String cityCodes = busStopService.도시코드_조회(cityName);
        return ApiResponse.createSuccess(cityCodes);
    }

    @Description("정류소의 노드 ID 조회")
    @GetMapping("/node/id")
    public ApiResponse<String> getNodeId
        (
            @RequestParam String cityCode, // 도시 코드
            @RequestParam String name // 정류소 이름
        ) throws UnsupportedEncodingException {

        String nodeId = busStopService.버스정류장_노드_ID_조회(cityCode, name);
        return ApiResponse.createSuccess(nodeId);
    }
}
