package com.example.busnotice.domain.bus;

import com.example.busnotice.domain.bus.res.BusArrInfosDto;
import com.example.busnotice.domain.bus.res.BusArrInfosDto.Item;
import com.example.busnotice.domain.bus.res.BusInfosDto;
import com.example.busnotice.domain.bus.res.BusNameAndTypeResponse;
import com.example.busnotice.domain.bus.res.SeoulBusArrInfosDto;
import com.example.busnotice.domain.bus.res.SeoulBusInfosDto;
import com.example.busnotice.domain.bus.res.SeoulBusInfosDto.BusRoute;
import com.example.busnotice.domain.busStop.BusStopService;
import com.example.busnotice.global.code.StatusCode;
import com.example.busnotice.global.exception.BusStopException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusService {

    @Value("${open-api.service.key}")
    private String serviceKey;

    private final WebClient webClient;
    private final BusStopService busStopService;

    public List<Item> 특정_노드_ID에_도착하는_모든_버스들_정보_조회(String cityName, String nodeId)
        throws UnsupportedEncodingException {
        String cityCode = busStopService.도시코드_DB_조회(cityName);
        // 서울의 경우는 따로 처리
        if (cityCode.equals("11")) {
            String url = "http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid";
            String encodedNodeId = URLEncoder.encode(nodeId, StandardCharsets.UTF_8.toString());
            String encodedServiceKey = URLEncoder.encode(serviceKey,
                StandardCharsets.UTF_8.toString());
            URI uri = URI.create(
                String.format("%s?serviceKey=%s&arsId=%s&resultType=json", url,
                    encodedServiceKey, encodedNodeId));

            // WebClient 호출 - 서울의 해당 노드 ID 에 도착하는 모든 버스들 조회 (운행종료 포함)
            SeoulBusArrInfosDto result = webClient.get().uri(uri).retrieve()
                .bodyToMono(SeoulBusArrInfosDto.class).block();
            System.out.println("(서울)특정_노드_ID에_도착하는_모든_버스들_정보_조회: " + result.toString());

            if (result.getMsgBody().getItemList() == null) {
                return Collections.emptyList();
            }

            List<SeoulBusArrInfosDto.Item> itemList = result.getMsgBody().getItemList();
            List<Item> items = itemList.stream().map(i -> i.toGeneralItem())
                .filter(Objects::nonNull).sorted(Comparator.comparingInt(Item::getArrtime))
                .toList();
            return items;
        }

        System.out.println("cityCode = " + cityCode);
        System.out.println("nodeId = " + nodeId);
        String url = "http://apis.data.go.kr/1613000/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList";
        String encodedCityCode = URLEncoder.encode(cityCode,
            StandardCharsets.UTF_8.toString());
        String encodedNodeId = URLEncoder.encode(nodeId, StandardCharsets.UTF_8.toString());
        String encodedServiceKey = URLEncoder.encode(serviceKey,
            StandardCharsets.UTF_8.toString());
        URI uri = URI.create(String.format("%s?serviceKey=%s&cityCode=%s&nodeId=%s&_type=json", url,
            encodedServiceKey, encodedCityCode, encodedNodeId));

        // WebClient 호출 - 해당 지역의 해당 노드 ID 에 도착 예정인 모든 버스들 조회
        BusArrInfosDto result = webClient.get().uri(uri).retrieve()
            .bodyToMono(BusArrInfosDto.class).block();
        System.out.println("(전국)특정_노드_ID에_도착하는_모든_버스들_정보_조회: " + result.toString());

        if (result.getResponse().getBody().getItems() == null) {
            return Collections.emptyList();
        }

        List<BusArrInfosDto.Item> items = result.getResponse().getBody().getItems()
            .getItem()
            .stream()
            .map(item -> {
                item.setRoutetp(item.getRoutetp().substring(0, 2));
                return item;
            })
            .toList();
        return items;
    }

    public List<Item> 특정_노드_ID에_도착하는_특정_버스들_정보_조회(String cityName, String nodeId,
        List<String> busList) throws UnsupportedEncodingException {
        List<Item> items = 특정_노드_ID에_도착하는_모든_버스들_정보_조회(cityName, nodeId);
        System.out.println("items = " + items);
        // 특정 routeno 에 해당하는 item 필터링
        List<BusArrInfosDto.Item> filteredItems = items.stream()
            .filter(item -> busList.contains(item.getRouteno())) // busList에 있는 routeno와 매칭
            .toList(); // 필터링 결과를 리스트로 변환

        return filteredItems;
    }

    public Item 특정_노드_ID에_가장_빨리_도착하는_버스_조회(String cityName, String nodeId, List<String> busList)
        throws UnsupportedEncodingException {
        List<Item> items = 특정_노드_ID에_도착하는_특정_버스들_정보_조회(cityName, nodeId, busList);
        Item fastestBus = items.stream()
            .min(Comparator.comparingInt(Item::getArrtime)) // arrtime 기준으로 최소값 찾기
            .orElse(null);
        return fastestBus;
    }

    public List<Item> 특정_노드_ID에_가장_빨리_도착하는_첫번째_두번째_버스_조회(String cityName, String nodeId,
        List<String> busList) throws UnsupportedEncodingException {
        List<Item> items = 특정_노드_ID에_도착하는_특정_버스들_정보_조회(cityName, nodeId, busList);
        List<Item> sortedItems = items.stream().sorted(Comparator.comparingInt(Item::getArrtime))
            .collect(Collectors.toList());
        return sortedItems.stream().limit(2).collect(Collectors.toList());
    }

    @Cacheable(value = "busNames_through_stn", key = "#p0 + '_' + #p1")
    public List<BusNameAndTypeResponse> 특정_노드_ID를_경유하는_모든_버스들_이름_조회(String cityName, String nodeId)
        throws UnsupportedEncodingException {
        log.info("{}_{} 를 경유하는 모든 버스들 이름 캐싱 실패, 메서드 실행", cityName, nodeId);
        String cityCode = busStopService.도시코드_DB_조회(cityName);

        // 서울인 경우만 따로 처리
        if (cityCode.equals("11")) {
            String url = "http://ws.bus.go.kr/api/rest/stationinfo/getRouteByStation";
            String encodedNodeId = URLEncoder.encode(nodeId, StandardCharsets.UTF_8.toString());
            String encodedServiceKey = URLEncoder.encode(serviceKey,
                StandardCharsets.UTF_8.toString());
            URI uri = URI.create(
                String.format("%s?serviceKey=%s&arsId=%s&resultType=json", url,
                    encodedServiceKey, encodedNodeId));

            // WebClient 호출 - 서울의 해당 노드 ID 를 경유하는 노선 정보 조회
            SeoulBusInfosDto result = webClient.get().uri(uri).retrieve()
                .bodyToMono(SeoulBusInfosDto.class).block();
            log.info("SeoulBusInfosDto: {}", result);
            List<BusRoute> busRoutes = result.getMsgBody().getItemList();
            if (busRoutes == null || busRoutes.isEmpty()) {
                return Collections.emptyList();
            }
            List<BusNameAndTypeResponse> list = result.getMsgBody().getItemList().stream()
                .map(i -> {
                        String routeType = "";
                        switch (i.getBusRouteType()) {
                            case 1:
                                routeType = "공항";
                                break;
                            case 2:
                                routeType = "마을";
                                break;
                            case 3:
                                routeType = "간선";
                                break;
                            case 4:
                                routeType = "지선";
                                break;
                            case 5:
                                routeType = "순환";
                                break;
                            case 6:
                                routeType = "광역";
                                break;
                            case 7:
                                routeType = "인천";
                                break;
                            case 8:
                                routeType = "경기";
                                break;
                            case 9:
                                routeType = "폐지";
                                break;
                            case 0:
                                routeType = "공용";
                                break;
                            default:
                                routeType = "알수없음"; // 예외 처리
                        }
                        return new BusNameAndTypeResponse(i.getBusRouteNm(), routeType);
                    }
                ).toList();
            return list;
        }

        String url = "http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnThrghRouteList";
        String encodedCityCode = URLEncoder.encode(cityCode,
            StandardCharsets.UTF_8.toString());
        String encodedNodeId = URLEncoder.encode(nodeId, StandardCharsets.UTF_8.toString());
        String encodedServiceKey = URLEncoder.encode(serviceKey,
            StandardCharsets.UTF_8.toString());
        URI uri = URI.create(
            String.format("%s?serviceKey=%s&cityCode=%s&nodeid=%s&numOfRows=20&_type=json", url,
                encodedServiceKey, encodedCityCode, encodedNodeId));

        // WebClient 호출 - 해당 지역의 해당 노드 ID 를 경유하는 노선 정보 조회
        BusInfosDto result = webClient.get().uri(uri).retrieve()
            .bodyToMono(BusInfosDto.class).block();
        log.info("BusInfosDto: {}", result);

        if (
            result.getResponse().getBody().getItems() == null
            || result.getResponse().getBody().getItems().getItem() == null
            || result.getResponse().getBody().getItems().getItem().isEmpty()
        ) {
            return Collections.emptyList();
        }

        // routeno 리스트 추출
        List<BusNameAndTypeResponse> list = result.getResponse().getBody().getItems().getItem()
            .stream()
            .map(i -> new BusNameAndTypeResponse(i.getRouteNo(), i.getRouteTp().substring(0, 2))
            ).toList();
        return list;
    }

}
