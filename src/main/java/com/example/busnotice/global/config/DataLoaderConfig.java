package com.example.busnotice.global.config;

import com.example.busnotice.domain.busStop.CityCode;
import com.example.busnotice.domain.busStop.CityCodeRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoaderConfig {

    @Bean
    public ApplicationRunner dataLoader(CityCodeRepository cityCodeRepository) {
        return args -> {
            // 도시 코드와 이름 리스트
            List<CityCode> cityCodes = Arrays.asList(
                new CityCode("11", "서울특별시"),
                new CityCode("21", "부산광역시"),
                new CityCode("22", "대구광역시"),
                new CityCode("23", "인천광역시"),
                new CityCode("24", "광주광역시"),
                new CityCode("25", "대전광역시"),
                new CityCode("26", "울산광역시"),
                new CityCode("39", "제주도"),
                new CityCode("31010", "수원시"),
                new CityCode("31020", "성남시"),
                new CityCode("31030", "의정부시"),
                new CityCode("31040", "안양시"),
                new CityCode("31050", "부천시"),
                new CityCode("31060", "광명시"),
                new CityCode("31070", "평택시"),
                new CityCode("31080", "동두천시"),
                new CityCode("31090", "안산시"),
                new CityCode("31100", "고양시"),
                new CityCode("31110", "과천시"),
                new CityCode("31120", "구리시"),
                new CityCode("31130", "남양주시"),
                new CityCode("31140", "오산시"),
                new CityCode("31150", "시흥시"),
                new CityCode("31160", "군포시"),
                new CityCode("31170", "의왕시"),
                new CityCode("31180", "하남시"),
                new CityCode("31190", "용인시"),
                new CityCode("31200", "파주시"),
                new CityCode("31210", "이천시"),
                new CityCode("31220", "안성시"),
                new CityCode("31230", "김포시"),
                new CityCode("31240", "화성시"),
                new CityCode("31250", "광주시"),
                new CityCode("31260", "양주시"),
                new CityCode("31270", "포천시"),
                new CityCode("32010", "춘천시"),
                new CityCode("32020", "원주시"),
                new CityCode("32050", "태백시"),
                new CityCode("34010", "천안시"),
                new CityCode("34020", "공주시"),
                new CityCode("34040", "아산시"),
                new CityCode("34050", "서산시"),
                new CityCode("34060", "논산시"),
                new CityCode("34070", "계룡시"),
                new CityCode("35010", "전주시"),
                new CityCode("35020", "군산시"),
                new CityCode("35040", "정읍시"),
                new CityCode("35050", "남원시"),
                new CityCode("35060", "김제시"),
                new CityCode("36010", "목포시"),
                new CityCode("36020", "여수시"),
                new CityCode("36030", "순천시"),
                new CityCode("36040", "나주시"),
                new CityCode("36060", "광양시"),
                new CityCode("37010", "포항시"),
                new CityCode("37020", "경주시"),
                new CityCode("37030", "김천시"),
                new CityCode("37040", "안동시"),
                new CityCode("37050", "구미시"),
                new CityCode("37060", "영주시"),
                new CityCode("37070", "영천시"),
                new CityCode("37080", "상주시"),
                new CityCode("37090", "문경시"),
                new CityCode("37100", "경산시"),
                new CityCode("38010", "창원시"),
                new CityCode("38030", "진주시"),
                new CityCode("38050", "통영시"),
                new CityCode("38060", "사천시"),
                new CityCode("38070", "김해시"),
                new CityCode("38080", "밀양시"),
                new CityCode("38090", "거제시"),
                new CityCode("38100", "양산시")
            );

            // 중복 삽입 방지 및 저장
            cityCodes.forEach(cityCode -> {
                if (!cityCodeRepository.existsByCode(cityCode.getCode())) {
                    cityCodeRepository.save(cityCode);
                }
            });
        };
    }
}
