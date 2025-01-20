package com.example.busnotice;


import com.example.busnotice.domain.bus.BusService;
import com.example.busnotice.domain.bus.res.BusStationResponse;
import com.example.busnotice.domain.bus.res.BusStationResponse.Item;
import com.example.busnotice.domain.bus.res.BusStationResponse.Items;
import com.example.busnotice.global.code.StatusCode;
import com.example.busnotice.global.exception.BusStopException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
@Transactional
public class BusTest {

    @Value("${bus.station.info.inquire.service.key}")
    private String busStationInfoServiceKey;

    @Autowired
    BusService busService;
    @Autowired
    WebClient webClient;

    @Test
    void getNodeId() throws UnsupportedEncodingException {
        String url = "http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnNoList";
        String encodedCityCode = URLEncoder.encode(String.valueOf("22"), StandardCharsets.UTF_8.toString());
        String encodedName = URLEncoder.encode("경북대학교북문건너", StandardCharsets.UTF_8.toString());
        String encodedServiceKey = URLEncoder.encode(busStationInfoServiceKey, StandardCharsets.UTF_8.toString());
        URI uri = URI.create(String.format("%s?serviceKey=%s&cityCode=%s&nodeNm=%s&_type=json",
            url, encodedServiceKey, encodedCityCode, encodedName));

        // WebClient 호출
        BusStationResponse result = webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(BusStationResponse.class)
            .block();
        Items items = result.getResponse().getBody().getItems();
        if (items == null || items.getItem().isEmpty()) {
            throw new BusStopException(StatusCode.NOT_FOUND, "해당 이름을 포함하는 버스정류장이 존재하지 않습니다.");
        }
        List<Item> itemsList = items.getItem();
        if (itemsList.size() >= 2) {
            throw new BusStopException(StatusCode.BAD_REQUEST, "해당 이름을 포함하는 버스정류장이 2개 이상입니다.");
        }
        System.out.println("노드 ID: " + itemsList.get(0).getNodeid());
    }

}
