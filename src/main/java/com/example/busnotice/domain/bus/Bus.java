package com.example.busnotice.domain.bus;

import com.example.busnotice.domain.busStop.BusStopSection;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bus_stop_section_id", nullable = false)
    private BusStopSection busStopSection;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    public Bus() {
    }

    public Bus(BusStopSection busStopSection, String name, String type) {
        this.busStopSection = busStopSection;
        this.name = name;
        this.type = type;
    }

    public static Bus toEntity(BusStopSection busStopSection, String name, String type) {
        return new Bus(busStopSection, name, type);
    }
}
