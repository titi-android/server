package com.example.busnotice.domain.bus;

import com.example.busnotice.domain.busStop.BusStop;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
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
    @JoinColumn(name = "busStop_id")
    private BusStop busStop;

    @Column
    private String name;

    public Bus() {

    }

    public Bus(BusStop busStop, String name) {

    }

    public static Bus toEntity(BusStop busStop, String name) {
        return new Bus(busStop, name);
    }
}
