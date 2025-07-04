package com.example.busnotice.domain.subway.dto;

// MergedStationDto.java

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor // 역직렬화 시 꼭 필요
@AllArgsConstructor
@Getter
@Setter
public class MergedStationDto {

    private String stationName;
    private CoordinateDto coordinate;
    private List<String> lines;
}