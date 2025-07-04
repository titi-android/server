package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.bus.Bus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class BusStopSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String regionName;

    @Column(nullable = false)
    private String busStopName;

    @Column(nullable = false)
    private String nodeId;

    @OneToMany(mappedBy = "busStopSection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bus> busList;

    public BusStopSection() {}

    public BusStopSection(String regionName, String busStopName, String nodeId, List<Bus> busList) {
        this.regionName = regionName;
        this.busStopName = busStopName;
        this.nodeId = nodeId;
        this.busList = busList;
    }
}