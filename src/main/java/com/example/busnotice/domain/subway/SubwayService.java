package com.example.busnotice.domain.subway;

import com.example.busnotice.domain.subway.dto.CoordinateDto;
import com.example.busnotice.domain.subway.dto.MergedStationDto;
import com.example.busnotice.domain.subway.dto.SubwayStationResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SubwayService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${open-api.subway.key}")
    private String apiKey;

    private static final String BASE_URL =
        "https://t-data.seoul.go.kr/apig/apiman-gateway/tapi/TaimsKsccDvSubwayStationGeom/1.0?apikey=%s";

    @Cacheable(value = "subwayStations", key = "#p0")
    public List<MergedStationDto> fetchMergedStationList(String stName) {
        String url = String.format(BASE_URL, apiKey);

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

}
