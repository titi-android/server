package com.example.busnotice;


import com.example.busnotice.domain.bus.BusService;
import com.example.busnotice.domain.busStop.CityCodeRepository;
import com.example.busnotice.domain.busStop.res.BusStopsDto;
import com.example.busnotice.domain.busStop.res.BusStopsDto.Item;
import com.example.busnotice.domain.busStop.res.BusStopsDto.Items;
import com.example.busnotice.global.code.StatusCode;
import com.example.busnotice.global.exception.BusStopException;
import com.example.busnotice.global.exception.GeneralException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
@Transactional
public class BusTest {

    @Value("${open-api.service.key}")
    private String busStationInfoServiceKey;

    @Autowired
    BusService busService;
    @Autowired
    CityCodeRepository cityCodeRepository;
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
        BusStopsDto result = webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(BusStopsDto.class)
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

    @Test
    public void 도시코드_DB_조회() {
        String cityCode = cityCodeRepository.findByName("광주시")
            .orElseThrow(() -> new GeneralException(StatusCode.BAD_REQUEST, "해당 지역이 존재하지 않습니다."))
            .getCode();
        System.out.println("cityCode = " + cityCode);
    }
}
