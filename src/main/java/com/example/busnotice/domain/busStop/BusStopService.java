package com.example.busnotice.domain.busStop;

import com.example.busnotice.domain.bus.res.BusStationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class BusStopService {

    @Value("${bus.station.info.inquire.service.key}")
    private String busStationInfoServiceKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BusStopRepository busStopRepository;

    public String getCityCode(String cityName) throws UnsupportedEncodingException {
        String url = "http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getCtyCodeList";
        String encodedServiceKey = URLEncoder.encode(busStationInfoServiceKey, StandardCharsets.UTF_8.toString());
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

            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error while processing the response";
        }
    }
}
