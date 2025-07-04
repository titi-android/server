package com.example.busnotice.domain.subway.dto;

import lombok.Data;

// SubwayStationOfLineDto.java
@Data
public class SubwayStationOfLineDto {
    private String stationCd;
    private String stationNm;
    private String stationNmEng;
    private String lineNum;
    private String frCode;

    // 기본 생성자
    public SubwayStationOfLineDto() {
    }

    // 필요하다면 전체 필드 생성자도 추가
    public SubwayStationOfLineDto(String stationCd, String stationNm, String stationNmEng, String lineNum, String frCode) {
        this.stationCd = stationCd;
        this.stationNm = stationNm;
        this.stationNmEng = stationNmEng;
        this.lineNum = lineNum;
        this.frCode = frCode;
    }
}
