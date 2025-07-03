package com.example.busnotice.domain.subway.dto;

public record SubwayStationDto(
    String stationCd,
    String stationNm,
    String lineNum,
    String frCode,
    String stationLat,
    String stationLng
) {}
