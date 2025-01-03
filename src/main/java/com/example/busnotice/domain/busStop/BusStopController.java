package com.example.busnotice.domain.busStop;

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
    public String getCityCode(@RequestParam String cityName) throws UnsupportedEncodingException {
        String cityCodes = busStopService.getCityCode(cityName);
        return cityCodes;
    }
}
