package com.example.busnotice.domain.subway;

import com.example.busnotice.domain.subway.dto.MergedStationDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SubwayController {

    private final SubwayService subwayService;

    @GetMapping("/subway/stations")
    public List<MergedStationDto> getSubwayStations(@RequestParam(name = "stName", required = false) String stName) {
        return subwayService.fetchMergedStationList(stName);
    }
}
