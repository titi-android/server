package com.example.busnotice.domain.subway;

import com.example.busnotice.domain.subway.dto.*;
import com.example.busnotice.util.SubwayLineMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubwaySectionService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${open-api.subway.key}")
    private String apiKey;

    @Value("${seoul-open-api.subway.key}")
    private String seoulApiKey;

    @Value("${open-api.normal.key}")
    private String normalKey;

    @Cacheable(value = "subwayStations", key = "#p0")
    public List<MergedStationDto> fetchMergedStationList(String stName) {
        String url = String.format("https://t-data.seoul.go.kr/apig/apiman-gateway/tapi/TaimsKsccDvSubwayStationGeom/1.0?apikey=%s", seoulApiKey);

        ResponseEntity<List<SubwayStationResponse>> response =
                restTemplate.exchange(
                        url,
                        org.springframework.http.HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {
                        }
                );

        List<SubwayStationResponse> rawList = response.getBody();
        if (rawList == null) {
            return Collections.emptyList();
        }

        // 중복 방지용 Set 임시로 사용
        Map<String, Set<String>> lineSetMap = new HashMap<>();
        Map<String, CoordinateDto> coordMap = new HashMap<>();

        for (SubwayStationResponse station : rawList) {
            if (station.stationName() == null || !station.stationName().contains(stName)) {
                continue;
            }

            String name = station.stationName();
            lineSetMap.computeIfAbsent(name, k -> new HashSet<>()).add(station.lineName());
            coordMap.putIfAbsent(name, new CoordinateDto(station.latitude(), station.longitude()));
        }

        // 최종 DTO 리스트
        return lineSetMap.entrySet().stream()
                .map(entry -> new MergedStationDto(
                        entry.getKey(),
                        coordMap.get(entry.getKey()),
                        new ArrayList<>(entry.getValue())  // 여기서 Set → List 변환
                ))
                .collect(Collectors.toList());
    }

    // 특정 호선을 지나는 지하철역 리스트 반환
    @Cacheable(value = "subwayStationsOfLine", key = "#p0")
    public List<SubwayStationOfLineDto> getStationsOfLine(LineType lineType) {
        String url = String.format(
                "http://openapi.seoul.go.kr:8088/%s/json/SearchSTNBySubwayLineInfo/1/100/%%20/%%20/%s",
                URLEncoder.encode(normalKey,
                        StandardCharsets.UTF_8), URLEncoder.encode('0' + lineType.getDisplayName(),
                        StandardCharsets.UTF_8)
        );

        try {
            ResponseEntity<SearchSTNBySubwayLineInfoResponse> response = restTemplate.getForEntity(
                    url,
                    SearchSTNBySubwayLineInfoResponse.class
            );
            SearchSTNBySubwayLineInfoResponse result = response.getBody();
            if (result == null
                    || result.getSearchSTNBySubwayLineInfo() == null
                    || result.getSearchSTNBySubwayLineInfo().getRow() == null) {
                return Collections.emptyList();
            }

            return result.getSearchSTNBySubwayLineInfo().getRow().stream()
                    .map(row -> new SubwayStationOfLineDto(
                            row.getStationCd(),
                            row.getStationNm(),
                            row.getStationNmEng(),
                            row.getLineNum(),
                            row.getFrCode()
                    ))
                    .sorted(Comparator.comparing(dto -> Integer.parseInt(dto.getStationCd())))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // 예외 처리
            return Collections.emptyList();
        }
    }

    public List<RealtimeArrResponse.RealtimeArrival> getStationArrInfo(LineType lineType, String stName, LineDir lineDir) {
        String lineCodeByName = SubwayLineMapper.getLineCodeByName(lineType.get());
        System.out.println("lineCodeByName = " + lineCodeByName);
        System.out.println("stName = " + stName);

        String url = String.format(
                "http://swopenapi.seoul.go.kr/api/subway/%s/json/realtimeStationArrival/0/5/%s",
                apiKey, stName
        );

        RealtimeArrResponse response = restTemplate.getForObject(url, RealtimeArrResponse.class);
        if (response == null || response.getRealtimeArrivalList() == null) {
            return Collections.emptyList();
        }

        return response.getRealtimeArrivalList().stream()
                .filter(arrival -> lineCodeByName.equals(arrival.getSubwayId()))
                .filter(arrival -> lineDir.get().equals(arrival.getUpdnLine()))
                .collect(Collectors.toList());
    }
}
