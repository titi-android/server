package com.example.busnotice.domain.busStop;

import com.example.busnotice.domain.busStop.res.BusStopsDto;
import com.example.busnotice.domain.busStop.res.BusStopsDto.Item;
import com.example.busnotice.domain.busStop.res.BusStopsDto.Items;
import com.example.busnotice.domain.busStop.res.SeoulBusStopsDto;
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

    @Value("${open-api.service.key}")
    private String serviceKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CityCodeRepository cityCodeRepository;

    //    @Cacheable(value = "cityCodes", key = "#p0")
    public String 도시코드_API_조회(String cityName) throws UnsupportedEncodingException {
        // 서울인 경우만 따로 처리
        if (cityName.contains("서울")) {
            System.out.println(cityName + "의 도시 코드: " + "11");
            return "11";
        }

        log.info("{} 에 대한 도시코드 캐싱 실패, 메서드 실행", cityName);
        String url = "http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getCtyCodeList";
        String encodedServiceKey = URLEncoder.encode(serviceKey,
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

    @Cacheable(value = "cityCodes", key = "#p0")
    public String 도시코드_DB_조회(String cityName) {
        String cityCode = cityCodeRepository.findByName(cityName.trim())
            .orElseThrow(() -> new GeneralException(StatusCode.BAD_REQUEST, "해당 지역이 존재하지 않습니다."))
            .getCode();

        return cityCode;
    }

    public List<String> 해당_이름을_포함하는_버스정류장_목록_조회(String cityName, String busStopName)
        throws UnsupportedEncodingException {
        String cityCode = 도시코드_DB_조회(cityName);

        // 서울인 경우만 따로 처리
        if (cityCode.equals("11")) {
            String url = "http://ws.bus.go.kr/api/rest/stationinfo/getStationByName";
            String encodedServiceKey = URLEncoder.encode(serviceKey,
                StandardCharsets.UTF_8.toString());
            String endCodedBusStopName = URLEncoder.encode(busStopName,
                StandardCharsets.UTF_8.toString());
            URI uri = URI.create(String.format("%s?serviceKey=%s&stSrch=%s&resultType=json",
                url, encodedServiceKey, endCodedBusStopName));

            // WebClient 호출 - 서울의 해당 이름이 포함된 버스정류장을 모두 조회
            SeoulBusStopsDto response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(SeoulBusStopsDto.class)
                .block();
            System.out.println("response.toString() = " + response.toString());
            if (response.getMsgBody().getItemList() == null || response.getMsgBody().getItemList()
                .isEmpty()) {
                throw new BusStopException(StatusCode.NOT_FOUND, "해당 이름을 포함하는 버스정류장이 존재하지 않습니다.");
            }
            List<String> busNames = response.getMsgBody().getItemList().stream()
                .map(item -> item.getStNm()).toList();
            return busNames;
        }

        cityCode = cityCode.trim().replaceAll("\\s+", "");
        busStopName = busStopName.trim().replaceAll("\\s+", "");

        String url = "http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnNoList";
        String encodedCityCode = URLEncoder.encode(cityCode,
            StandardCharsets.UTF_8.toString());
        String encodedName = URLEncoder.encode(busStopName, StandardCharsets.UTF_8.toString());
        String encodedNumOfRows = URLEncoder.encode(String.valueOf(100), StandardCharsets.UTF_8.toString());
        String encodedServiceKey = URLEncoder.encode(serviceKey,
            StandardCharsets.UTF_8.toString());
        URI uri = URI.create(String.format("%s?serviceKey=%s&cityCode=%s&nodeNm=%s&numOfRows=%s&_type=json", url,
            encodedServiceKey, encodedCityCode, encodedName, encodedNumOfRows));

        // WebClient 호출 - 해당 지역의 해당 이름이 포함된 버스정류장을 모두 조회
        BusStopsDto result = webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(BusStopsDto.class)
            .block();
        System.out.println("result.toString() = " + result.toString());
        Items items = result.getResponse().getBody().getItems();
        if (items == null || items.getItem().isEmpty()) {
            throw new BusStopException(StatusCode.NOT_FOUND, "해당 이름을 포함하는 버스정류장이 존재하지 않습니다.");
        }
        List<Item> itemsList = items.getItem();
        return itemsList.stream().map(item -> item.getNodenm()).toList();
    }

    @Cacheable(value = "nodeIds", key = "#p0 + '_' + #p1")
    public String 버스정류장_노드_ID_조회(String cityName, String busStopName)
        throws IOException {
        String cityCode = 도시코드_DB_조회(cityName);

        // 서울인 경우만 따로 처리
        if (cityCode.equals("11")) {
            String url = "http://ws.bus.go.kr/api/rest/stationinfo/getStationByName";
            String encodedServiceKey = URLEncoder.encode(serviceKey,
                StandardCharsets.UTF_8.toString());
            String endCodedBusStopName = URLEncoder.encode(busStopName,
                StandardCharsets.UTF_8.toString());
            URI uri = URI.create(String.format("%s?serviceKey=%s&stSrch=%s&resultType=json",
                url, encodedServiceKey, endCodedBusStopName));

            // WebClient 호출 - 서울의 해당 이름이 포함된 버스정류장을 모두 조회
            SeoulBusStopsDto response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(SeoulBusStopsDto.class)
                .block();
            System.out.println("response.toString() = " + response.toString());
            if (response.getMsgBody().getItemList() == null || response.getMsgBody().getItemList()
                .isEmpty()) {
                throw new BusStopException(StatusCode.NOT_FOUND, "해당 이름을 포함하는 버스정류장이 존재하지 않습니다.");
            }
            String nodeId = response.getMsgBody().getItemList().get(0).getArsId();
            return nodeId;
        }

        log.info("{}_{} 에 대한 노드 ID 캐싱 실패, 메서드 실행", cityCode, busStopName);
        cityCode = cityCode.trim().replaceAll("\\s+", "");
        busStopName = busStopName.trim().replaceAll("\\s+", "");

        String url = "http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnNoList";
        String encodedCityCode = URLEncoder.encode(String.valueOf(cityCode),
            StandardCharsets.UTF_8.toString());
        String encodedName = URLEncoder.encode(busStopName, StandardCharsets.UTF_8.toString());
        String encodedServiceKey = URLEncoder.encode(serviceKey,
            StandardCharsets.UTF_8.toString());
        URI uri = URI.create(String.format("%s?serviceKey=%s&cityCode=%s&nodeNm=%s&_type=json", url,
            encodedServiceKey, encodedCityCode, encodedName));

        // WebClient 호출 - 해당 지역의 해당 이름이 포함된 버스정류장을 모두 조회
        BusStopsDto result = webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(BusStopsDto.class)
            .block();
        System.out.println("result.toString() = " + result.toString());
        Items items = result.getResponse().getBody().getItems();
        if (items == null || items.getItem().isEmpty()) {
            throw new BusStopException(StatusCode.NOT_FOUND, "해당 이름을 포함하는 버스정류장이 존재하지 않습니다.");
        }
        List<Item> itemsList = items.getItem();
        return itemsList.get(0).getNodeid();
    }
}
