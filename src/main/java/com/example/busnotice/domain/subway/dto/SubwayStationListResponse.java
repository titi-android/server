package com.example.busnotice.domain.subway.dto;

import java.util.List;

public record SubwayStationListResponse(
    List<SubwayStationDto> stationList
) {}