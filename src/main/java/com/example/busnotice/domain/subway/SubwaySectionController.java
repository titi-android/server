package com.example.busnotice.domain.subway;

import com.example.busnotice.domain.subway.dto.MergedStationDto;
import com.example.busnotice.domain.subway.dto.RealtimeArrResponse;
import com.example.busnotice.domain.subway.dto.SubwayStationOfLineDto;
import com.example.busnotice.global.format.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Tag(name = "Subway", description = "지하철 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v3")
public class SubwaySectionController {

    private final SubwaySectionService subwayService;

    @Operation(summary = "해당 검색어를 포함하는 지하철역들의 호선 정보 및 좌표 반환. '역' 문구 제외하고 입력")
    @GetMapping("/subway/stations")
    public ApiResponse<List<MergedStationDto>> getSubwayStations(@RequestParam(name = "stName", required = false) String stName) {
        List<MergedStationDto> response = subwayService.fetchMergedStationList(stName);
        return ApiResponse.createSuccessWithData(response, "검색어 기반 지하철역 노선 및 위치 정보 반환 성공");
    }

    @Operation(summary = "특정 노선을 지나는 지하철역 리스트 반환")
    @GetMapping("/subway/line")
    public ApiResponse<List<SubwayStationOfLineDto>> getStationsOfLine(@RequestParam(name = "lineName") LineType lineType) throws UnsupportedEncodingException {
        List<SubwayStationOfLineDto> response = subwayService.getStationsOfLine(lineType);
        return ApiResponse.createSuccessWithData(response, "해당 노선의 지하철역 리스트 반환 성공");
    }

    @Operation(summary = "특정 노선의 지하철역의 실시간 도착 정보 반환. '역' 문구 제외하고 입력.")
    @GetMapping("/subway/line/station")
    public ApiResponse<List<RealtimeArrResponse.RealtimeArrival>> getStationArrInfo(
            @RequestParam(name = "lineType") LineType lineType, // 호선명
            @RequestParam(name = "stName") String stName, // 해당 호선 내 지하철역명
            @RequestParam(name = "lineDir") LineDir dir // 상행 또는 하행
    ) {
        List<RealtimeArrResponse.RealtimeArrival> response = subwayService.getStationArrInfo(lineType, stName, dir);
        return ApiResponse.createSuccessWithData(response, "실시간 도착 정보 반환 성공");
    }
}
