package com.example.busnotice.domain.busStop;

import com.example.busnotice.domain.bus.res.BusStationResponse;
import com.example.busnotice.domain.bus.res.BusStationResponse.Item;
import com.example.busnotice.domain.bus.res.BusStationResponse.Items;
import com.example.busnotice.global.code.StatusCode;
import com.example.busnotice.global.exception.BusStopException;
import com.example.busnotice.global.exception.GeneralException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusStopService {

    @Value("${bus.station.info.inquire.service.key}")
    private String busStationInfoServiceKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Cacheable(value = "cityCodes", key = "#p0")
    public String 도시코드_조회(String cityName) throws UnsupportedEncodingException {
        log.info("{} 에 대한 도시코드 캐싱 실패, 메서드 실행", cityName);
        String url = "http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getCtyCodeList";
        String encodedServiceKey = URLEncoder.encode(busStationInfoServiceKey,
            StandardCharsets.UTF_8.toString());
        URI uri = URI.create(String.format("%s?serviceKey=%s&_type=json",
            url, encodedServiceKey));

        // WebClient 호출
        String response = webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(String.class)
            .block();

        try {
            // JSON 파싱
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

            // cityname에 특정 문자열이 포함된 항목의 citycode 찾기
            StringBuilder result = new StringBuilder();
            for (JsonNode item : itemsNode) {
                String cityNameText = item.path("cityname").asText();
                if (cityNameText.contains(cityName)) {
                    String cityCode = item.path("citycode").asText();
                    System.out.println(cityName + "의 도시 코드: " + cityCode);
                    result.append(cityCode).append("\n");
                }
            }
            String cityCode = result.toString().trim();
            if (cityCode == null || cityCode.isEmpty()) {
                throw new GeneralException(StatusCode.BAD_REQUEST, "도시 코드 조회에 실패했습니다.");
            }
            return result.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeneralException(StatusCode.INTERNAL_SERVER_ERROR, "도시 코드 조회에 실패했습니다.");
        }
    }


    @Cacheable(value = "nodeIds", key = "#p0 + '_' + #p1")
    public String 버스정류장_노드_ID_조회(String cityCode, String busStopName)
        throws IOException {
        log.info("{}_{} 에 대한 노드 ID 캐싱 실패, 메서드 실행", cityCode, busStopName);
        cityCode = cityCode.trim().replaceAll("\\s+", "");
        busStopName = busStopName.trim().replaceAll("\\s+", "");

        String url = "http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnNoList";
        String encodedCityCode = URLEncoder.encode(String.valueOf(cityCode),
            StandardCharsets.UTF_8.toString());
        String encodedName = URLEncoder.encode(busStopName, StandardCharsets.UTF_8.toString());
        String encodedServiceKey = URLEncoder.encode(busStationInfoServiceKey,
            StandardCharsets.UTF_8.toString());
        URI uri = URI.create(String.format("%s?serviceKey=%s&cityCode=%s&nodeNm=%s&_type=json", url,
            encodedServiceKey, encodedCityCode, encodedName));

        // WebClient 호출
        BusStationResponse result = webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(BusStationResponse.class)
            .block();
        System.out.println("result.toString() = " + result.toString());
        Items items = result.getResponse().getBody().getItems();
        if (items == null || items.getItem().isEmpty()) {
            throw new BusStopException(StatusCode.NOT_FOUND, "해당 이름을 포함하는 버스정류장이 존재하지 않습니다.");
        }
        List<Item> itemsList = items.getItem();
        if (itemsList.size() >= 2) {
            throw new BusStopException(StatusCode.BAD_REQUEST, "해당 이름을 포함하는 버스정류장이 2개 이상입니다.");
        }
        return itemsList.get(0).getNodeid();
    }
}
