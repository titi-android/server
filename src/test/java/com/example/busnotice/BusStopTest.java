package com.example.busnotice;

import com.example.busnotice.domain.bus.res.BusInfosDto;
import com.example.busnotice.domain.bus.res.BusArrInfosDto;
import com.example.busnotice.domain.bus.res.BusArrInfosDto.Item;
import com.example.busnotice.domain.bus.res.SeoulBusArrInfosDto;
import com.example.busnotice.domain.bus.res.SeoulBusInfosDto;
import com.example.busnotice.domain.busStop.res.SeoulBusStopsDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jdk.jfr.Description;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
@Transactional
public class BusStopTest {

    @Value("${open-api.service.key}")
    private String serviceKey;
    @Autowired
    WebClient webClient;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    @Description("특정 node id 에 도착하는 모든 버스들 조회")
    void getAllBusInfoOfBusSt() throws UnsupportedEncodingException {
        String cityCode = "22"; // 대구
        String nodeId = "DGB7021025900"; // node id
        List<Item> items = 특정_노드_ID에_도착하는_모든_버스들_정보_조회(cityCode, nodeId);
        for (Item item : items) {
            System.out.println("item = " + item);
        }
    }

    @Test
    @Description("특정 node id 에 도착하는 특정 버스들 조회")
    void getSpecBusInfoOfBusSt() throws UnsupportedEncodingException {
        String cityCode = "22"; // 대구
        String nodeId = "DGB7021025900"; // node id
        List<String> busList = new ArrayList<>();
        busList.add("306");
        busList.add("410-1");
        busList.add("급행6");

        List<Item> items = 특정_노드_ID에_도착하는_특정_버스들_정보_조회(cityCode, nodeId, busList);
        for (Item item : items) {
            System.out.println("item = " + item);
        }
    }


    @Test
    @Description("특정 node id 에 도착하는 특정 버스들 중 가장 빨리 도착하는 버스 조회")
    void getMinArrBusInfoOfBusSt() throws UnsupportedEncodingException {
        String cityCode = "22"; // 대구
        String nodeId = "DGB7021025900"; // node id
        List<String> busList = new ArrayList<>();
        busList.add("306");
        busList.add("410-1");
        busList.add("급행6");

        // 특정 노드 ID에 도착하는 버스들 조회
        List<Item> items = 특정_노드_ID에_도착하는_특정_버스들_정보_조회(cityCode, nodeId, busList);

        // 가장 빨리 도착하는 버스 찾기
        Item fastestBus = 가장_빨리_도착하는_버스_조회(items);

        // 결과 출력
        if (fastestBus != null) {
            System.out.println("가장 빨리 도착하는 버스 정보:");
            System.out.println("노선 번호: " + fastestBus.getRouteno());
            System.out.println("남은 정류장 수: " + fastestBus.getArrprevstationcnt());
            System.out.println("도착 예정 시간(초): " + fastestBus.getArrtime());
        } else {
            System.out.println("조회된 버스가 없습니다.");
        }
    }

    @Test
    public void getAllBusNamesOfBusSt()
        throws UnsupportedEncodingException, JsonProcessingException {
        String url = "http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnThrghRouteList";
        String encodedCityCode = URLEncoder.encode(String.valueOf("22"),
            StandardCharsets.UTF_8.toString());
        String encodedNodeId = URLEncoder.encode("DGB7021025900",
            StandardCharsets.UTF_8.toString());
        String encodedServiceKey = URLEncoder.encode(serviceKey,
            StandardCharsets.UTF_8.toString());
        URI uri = URI.create(
            String.format("%s?serviceKey=%s&cityCode=%s&nodeid=%s&numOfRows=20&_type=json",
                url, encodedServiceKey, encodedCityCode, encodedNodeId));

        // WebClient 호출
        BusInfosDto result = webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(BusInfosDto.class)
            .block();

        // routeno 리스트 추출
        List<String> busNames = result.getResponse()
            .getBody()
            .getItems()
            .getItem()
            .stream()
            .map(BusInfosDto.Item::getRouteNo).toList();
        List<String> routeNos = busNames;
        System.out.println("routeNos = " + routeNos);
    }

    @Test
    void 서울_버스정류장_이름으로_노드_ID_조회() throws UnsupportedEncodingException {
        String url = "http://ws.bus.go.kr/api/rest/stationinfo/getStationByName";
        String encodedServiceKey = URLEncoder.encode(serviceKey,
            StandardCharsets.UTF_8.toString());
        String endCodedBusStopName = URLEncoder.encode("김동현체육공원",
            StandardCharsets.UTF_8.toString());
        URI uri = URI.create(String.format("%s?serviceKey=%s&stSrch=%s&resultType=json",
            url, encodedServiceKey, endCodedBusStopName));

        // WebClient 호출
        SeoulBusStopsDto response = webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(SeoulBusStopsDto.class)
            .block();
        System.out.println("response.toString() = " + response.toString());
//        System.out.println(response.getMsgBody().getItemList().get(0).getArsId());
    }

    @Test
    void 서울_특정_노선을_경유하는_버스_목록_조회() throws UnsupportedEncodingException {
        String url = "http://ws.bus.go.kr/api/rest/stationinfo/getRouteByStation";
        String encodedNodeId = URLEncoder.encode("03228", StandardCharsets.UTF_8.toString());
        String encodedServiceKey = URLEncoder.encode(serviceKey,
            StandardCharsets.UTF_8.toString());
        URI uri = URI.create(
            String.format("%s?serviceKey=%s&arsId=%s&resultType=json", url,
                encodedServiceKey, encodedNodeId));

        // WebClient 호출
        SeoulBusInfosDto result = webClient.get().uri(uri).retrieve()
            .bodyToMono(SeoulBusInfosDto.class).block();
        List<String> busNames = result.getMsgBody().getItemList().stream()
            .map(i -> i.getBusRouteNm()).toList();
        System.out.println("bus names: " + busNames);
    }

    @Test
    public void parseTest() {
        Pattern pattern = Pattern.compile("\\s*(\\d+)분\\s*(\\d+)초후\\s*\\[(\\d+)번째 전\\]\\s*");
        Matcher matcher = pattern.matcher("2분28초후[1번째 전]");

        if (matcher.find()) {
            int minutes = Integer.parseInt(matcher.group(1));
            int seconds = Integer.parseInt(matcher.group(2));
            int stopNumber = Integer.parseInt(matcher.group(3));

            int totalSeconds = (minutes * 60) + seconds;

            System.out.println("stopNumber = " + stopNumber);
            System.out.println("totalSeconds = " + totalSeconds);

        }
    }

    @Test
    void 서울_특정_노드_ID에_도착하는_모든_버스들_정보_조회() throws UnsupportedEncodingException {
//        String url = "http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid";
//        String encodedNodeId = URLEncoder.encode("03228", StandardCharsets.UTF_8.toString());
//        String encodedServiceKey = URLEncoder.encode(serviceKey,
//            StandardCharsets.UTF_8.toString());
//        URI uri = URI.create(
//            String.format("%s?serviceKey=%s&arsId=%s&resultType=json", url,
//                encodedServiceKey, encodedNodeId));
//
//        // WebClient 호출
//        SeoulBusStationArriveResponse result = webClient.get().uri(uri).retrieve()
//            .bodyToMono(SeoulBusStationArriveResponse.class).block();
//        List<SeoulBusStationArriveResponse.Item> itemList = result.getMsgBody().getItemList();
//        System.out.println("result.toString() = " + result.toString());

        String url = "http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid";
        String encodedNodeId = URLEncoder.encode("03228", StandardCharsets.UTF_8.toString());
        String encodedServiceKey = URLEncoder.encode(serviceKey,
            StandardCharsets.UTF_8.toString());
        URI uri = URI.create(
            String.format("%s?serviceKey=%s&arsId=%s&resultType=json", url,
                encodedServiceKey, encodedNodeId));

        // WebClient 호출
        SeoulBusArrInfosDto result = webClient.get().uri(uri).retrieve()
            .bodyToMono(SeoulBusArrInfosDto.class).block();
        System.out.println("result.toString() = " + result.toString());

        List<SeoulBusArrInfosDto.Item> itemList = result.getMsgBody().getItemList();
        List<Item> items = itemList.stream().map(i -> i.toGeneralItem()).filter(Objects::nonNull).sorted(Comparator.comparingInt(Item::getArrtime)).toList();

        System.out.println("items = " + items);
    }


    public List<Item> 특정_노드_ID에_도착하는_모든_버스들_정보_조회(
        String cityCode,
        String nodeId
    ) throws UnsupportedEncodingException {
        String url = "http://apis.data.go.kr/1613000/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList";
        String encodedCityCode = URLEncoder.encode(String.valueOf(cityCode),
            StandardCharsets.UTF_8.toString());
        String encodedNodeId = URLEncoder.encode(nodeId, StandardCharsets.UTF_8.toString());
        String encodedServiceKey = URLEncoder.encode(serviceKey,
            StandardCharsets.UTF_8.toString());
        URI uri = URI.create(String.format("%s?serviceKey=%s&cityCode=%s&nodeId=%s&_type=json",
            url, encodedServiceKey, encodedCityCode, encodedNodeId));

        // WebClient 호출
        BusArrInfosDto result = webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(BusArrInfosDto.class)
            .block();

        if (result != null && result.getResponse() != null &&
            result.getResponse().getBody() != null &&
            result.getResponse().getBody().getItems() != null) {

            List<BusArrInfosDto.Item> items = result.getResponse().getBody().getItems()
                .getItem();
            return items;
        } else {
            System.out.println("No data available.");
        }
        return null;
    }

    List<Item> 특정_노드_ID에_도착하는_특정_버스들_정보_조회(
        String cityCode,
        String nodeId,
        List<String> busList
    ) throws UnsupportedEncodingException {
        List<Item> items = 특정_노드_ID에_도착하는_모든_버스들_정보_조회(cityCode, nodeId);

        // 특정 routeno 에 해당하는 item 필터링
        List<BusArrInfosDto.Item> filteredItems = items.stream()
            .filter(item -> busList.contains(item.getRouteno())) // busList에 있는 routeno와 매칭
            .toList(); // 필터링 결과를 리스트로 변환

        return filteredItems;
    }

    Item 가장_빨리_도착하는_버스_조회(List<Item> items) {
        Item fastestBus = items.stream()
            .min(Comparator.comparingInt(Item::getArrtime)) // arrtime 기준으로 최소값 찾기
            .orElse(null);
        return fastestBus;
    }
}
