package com.example.busnotice.domain.schedule;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
class SubwaySection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String regionName;

    @Column(nullable = false)
    private String lineName; // 예: "2호선"

    @Column(nullable = false)
    private String stationName;

    private String dir;

    public SubwaySection() {
    }

    public SubwaySection(String regionName, String lineName, String stationName, String dir) {
        this.regionName = regionName;
        this.lineName = lineName;
        this.stationName = stationName;
        this.dir = dir;
    }
}