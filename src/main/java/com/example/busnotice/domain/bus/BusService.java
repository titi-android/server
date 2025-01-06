package com.example.busnotice.domain.bus;

import com.example.busnotice.domain.bus.res.BusStationArriveResponse;
import com.example.busnotice.domain.bus.res.BusStationArriveResponse.Item;
import com.example.busnotice.domain.bus.res.BusStationResponse;
import com.example.busnotice.domain.busStop.BusStop;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import jdk.jfr.Description;
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

    // 버스 정류장의 노드 id 조회
    public String getNodeId(String cityCode, String busStopName) throws UnsupportedEncodingException {
        cityCode = cityCode.trim().replaceAll("\\s+", "");
        busStopName = busStopName.trim().replaceAll("\\s+", "");

        String url = "http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnNoList";
        String encodedCityCode = URLEncoder.encode(String.valueOf(cityCode), StandardCharsets.UTF_8.toString());
        String encodedName = URLEncoder.encode(busStopName, StandardCharsets.UTF_8.toString());
        String encodedServiceKey = URLEncoder.encode(busStationInfoServiceKey, StandardCharsets.UTF_8.toString());
        URI uri = URI.create(String.format("%s?serviceKey=%s&cityCode=%s&nodeNm=%s&_type=json",
            url, encodedServiceKey, encodedCityCode, encodedName));

        // WebClient 호출
        BusStationResponse result = webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(BusStationResponse.class)
            .block();
        String nodeId = result.getResponse().getBody().getItems().getItem().getNodeid();
        System.out.println(busStopName + " 의 node id: " + nodeId);
        return nodeId;
    }

    // 특정 버스 정류장의 도착하는 버스 정보 조회
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

    // 특정 버스 정류장의 특정 버스들 중 가장 빨리 도착하는 버스 정보 조회

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

    @Description("버스 정류소 정보 배열에서 동일한 버스 중 가장 빠른 것들만 추출")
    public List<Item> getMinArrTimeItems(List<Item> items) {
        List<Item> minArrTimeItems = items.stream()
            .collect(Collectors.groupingBy(item -> item.getRouteid().substring(0, item.getRouteid().length() - 3)))
            .values().stream()
            .map(group -> group.stream()
                .min(Comparator.comparing(Item::getArrtime))
                .orElseThrow())
            .collect(Collectors.toList());
        return minArrTimeItems;
    }

//    public void getMinArrTimeBus(BusStop busStop, List<Bus> busList) {
//        getBusStationArriveInfo(busStop.getCityCode(), busStop.getNodeId(),)
//    }
}
