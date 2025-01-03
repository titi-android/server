package com.example.busnotice.domain.bus;

import com.example.busnotice.domain.bus.Response.BusStationArriveResponse;
import com.example.busnotice.domain.bus.Response.BusStationArriveResponse.Item;
import com.example.busnotice.domain.bus.Response.BusStationResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class BusService {

    @Value("${bus.station.info.inquire.service.key}")
    private String busStationInfoServiceKey;
    @Value("${bus.station.arrive.info.inquire.service.key}")
    private String busStationArriveInfoServiceKey;

    private final WebClient webClient;

    public BusStationResponse getBusStationInfo(Long cityCode, String name) throws UnsupportedEncodingException {
        String url = "http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnNoList";
        String encodedCityCode = URLEncoder.encode(String.valueOf(cityCode), StandardCharsets.UTF_8.toString());
        String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString());
        String encodedServiceKey = URLEncoder.encode(busStationInfoServiceKey, StandardCharsets.UTF_8.toString());
        URI uri = URI.create(String.format("%s?serviceKey=%s&cityCode=%s&nodeNm=%s&_type=json",
            url, encodedServiceKey, encodedCityCode, encodedName));

        // WebClient 호출
        BusStationResponse result = webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(BusStationResponse.class)
            .block();
        System.out.println("result = " + result);
        return result;
    }

    public BusStationArriveResponse getBusStationArriveInfo(Long cityCode, String nodeId) throws UnsupportedEncodingException {
        String url = "http://apis.data.go.kr/1613000/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList";
        String encodedCityCode = URLEncoder.encode(String.valueOf(cityCode), StandardCharsets.UTF_8.toString());
        String encodedNodeId = URLEncoder.encode(nodeId, StandardCharsets.UTF_8.toString());
        String encodedServiceKey = URLEncoder.encode(busStationArriveInfoServiceKey, StandardCharsets.UTF_8.toString());
        URI uri = URI.create(String.format("%s?serviceKey=%s&cityCode=%s&nodeId=%s&_type=json",
            url, encodedServiceKey, encodedCityCode, encodedNodeId));

        // WebClient 호출
        BusStationArriveResponse result = webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(BusStationArriveResponse.class)
            .block();
        return result;
    }

    public List<Item> filterArriveInfo(BusStationArriveResponse result, String[] routeNos) {
        List<Item> items = result.getResponse().getBody().getItems().getItem();
        List<Item> filteredItems = new ArrayList<>();
        List<String> routeNumList = Arrays.asList(routeNos);  // routeNums 배열을 리스트로 변환
        for (Item item : items) {
            if (routeNumList.contains(item.getRouteno())) {  // routeno가 routeNums 배열에 포함되는 경우
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }
}
