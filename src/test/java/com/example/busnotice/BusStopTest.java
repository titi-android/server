package com.example.busnotice;

import com.example.busnotice.domain.bus.res.BusStationArriveResponse;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @Value("${bus.station.info.inquire.service.key}")
    private String busStationInfoServiceKey;
    @Value("${bus.station.arrive.info.inquire.service.key}")
    private String busStationArriveInfoServiceKey;
    @Autowired
    WebClient webClient;

    @Test
    @Description("특정 node id 에 도착하는 모든 버스들 조회")
    void getAllBusInfoOfBusSt() throws UnsupportedEncodingException {
        String cityCode = "22";
        String nodeId = "DGB7021025900";
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
        System.out.println("result.toString() = " + result.toString());
    }

    @Test
    @Description("특정 node id 에 도착하는 특정 버스들 조회")
    void getSpecBusInfoOfBusSt() throws UnsupportedEncodingException {
        String cityCode = "22";
        String nodeId = "DGB7021025900";
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
        System.out.println("result.toString() = " + result.toString());
    }

    @Test
    @Description("특정 node id 에 도착하는 특정 버스들 중 가장 빨리 도착하는 버스 조회")
    void getMinArrBusInfoOfBusSt() throws UnsupportedEncodingException {
        String cityCode = "22";
        String nodeId = "DGB7021025900";
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
        System.out.println("result.toString() = " + result.toString());
    }
}
