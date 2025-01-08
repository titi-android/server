package com.example.busnotice.domain.bus;

import com.example.busnotice.domain.bus.res.BusStationArriveResponse;
import com.example.busnotice.domain.bus.res.BusStationArriveResponse.Item;
import com.example.busnotice.domain.bus.res.BusStationResponse;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
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

    // 버스 정류장의 노드 id 조회
    public String 버스정류장_노드_ID_조회(String cityCode, String busStopName)
        throws UnsupportedEncodingException {
        cityCode = cityCode.trim().replaceAll("\\s+", "");
        busStopName = busStopName.trim().replaceAll("\\s+", "");

        String url = "http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnNoList";
        String encodedCityCode = URLEncoder.encode(String.valueOf(cityCode),
            StandardCharsets.UTF_8.toString());
        String encodedName = URLEncoder.encode(busStopName, StandardCharsets.UTF_8.toString());
        String encodedServiceKey = URLEncoder.encode(busStationInfoServiceKey,
            StandardCharsets.UTF_8.toString());
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

    public List<Item> 특정_노드_ID에_도착하는_모든_버스들_정보_조회(
        String cityCode,
        String nodeId
    ) throws UnsupportedEncodingException {
        System.out.println("cityCode = " + cityCode);
        System.out.println("nodeId = " + nodeId);
        String url = "http://apis.data.go.kr/1613000/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList";
        String encodedCityCode = URLEncoder.encode(String.valueOf(cityCode),
            StandardCharsets.UTF_8.toString());
        String encodedNodeId = URLEncoder.encode(nodeId, StandardCharsets.UTF_8.toString());
        String encodedServiceKey = URLEncoder.encode(busStationArriveInfoServiceKey,
            StandardCharsets.UTF_8.toString());
        URI uri = URI.create(String.format("%s?serviceKey=%s&cityCode=%s&nodeId=%s&_type=json",
            url, encodedServiceKey, encodedCityCode, encodedNodeId));

        // WebClient 호출
        BusStationArriveResponse result = webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(BusStationArriveResponse.class)
            .block();

        if (result != null && result.getResponse() != null &&
            result.getResponse().getBody() != null &&
            result.getResponse().getBody().getItems() != null) {

            List<BusStationArriveResponse.Item> items = result.getResponse().getBody().getItems()
                .getItem();
            return items;
        } else {
            System.out.println("No data available.");
        }
        return null;
    }

    public List<Item> 특정_노드_ID에_도착하는_특정_버스들_정보_조회(
        String cityCode,
        String nodeId,
        List<String> busList
    ) throws UnsupportedEncodingException {
        List<Item> items = 특정_노드_ID에_도착하는_모든_버스들_정보_조회(cityCode, nodeId);
        System.out.println("items = " + items);
        // 특정 routeno 에 해당하는 item 필터링
        List<BusStationArriveResponse.Item> filteredItems = items.stream()
            .filter(item -> busList.contains(item.getRouteno())) // busList에 있는 routeno와 매칭
            .toList(); // 필터링 결과를 리스트로 변환

        return filteredItems;
    }

    public Item 특정_노드_ID에_가장_빨리_도착하는_버스_조회(String cityCode, String nodeId, List<String> busList)
        throws UnsupportedEncodingException {
        List<Item> items = 특정_노드_ID에_도착하는_특정_버스들_정보_조회(cityCode, nodeId, busList);
        Item fastestBus = items.stream()
            .min(Comparator.comparingInt(Item::getArrtime)) // arrtime 기준으로 최소값 찾기
            .orElse(null);
        return fastestBus;
    }

}
