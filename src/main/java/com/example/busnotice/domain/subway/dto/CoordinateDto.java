package com.example.busnotice.domain.subway.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// CoordinateDto.java
@NoArgsConstructor // Jackson 역직렬화 위해 필요
@AllArgsConstructor
@Getter
@Setter
public class CoordinateDto {
    private String x;
    private String y;
}