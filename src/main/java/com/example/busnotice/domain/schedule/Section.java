package com.example.busnotice.domain.schedule;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // "BUS" 또는 "SUBWAY"

    @Column(nullable = false)
    private int orderIndex;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    // 버스 구간일 때만 사용
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bus_stop_section_id")
    private BusStopSection busStopSection;

    // 지하철 구간일 때만 사용
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "subway_section_id")
    private SubwaySection subwaySection;

    public Section() {
    }

    public static Section busSection(int orderIndex, Schedule schedule, BusStopSection busStopSection) {
        Section section = new Section();
        section.type = "BUS";
        section.orderIndex = orderIndex;
        section.schedule = schedule;
        section.busStopSection = busStopSection;
        return section;
    }

    public static Section subwaySection(int orderIndex, Schedule schedule, SubwaySection subwaySection) {
        Section section = new Section();
        section.type = "SUBWAY";
        section.orderIndex = orderIndex;
        section.schedule = schedule;
        section.subwaySection = subwaySection;
        return section;
    }
}