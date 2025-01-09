package com.example.busnotice.domain.busStop;

import com.example.busnotice.domain.bus.Bus;
import com.example.busnotice.domain.schedule.Schedule;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class BusStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "busStop", fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(nullable = false)
    private String cityCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nodeId;

    @OneToMany(mappedBy = "busStop", cascade = CascadeType.REMOVE)
    private List<Bus> busList;

    public BusStop(String cityCode, String name, String nodeId) {
        this.cityCode = cityCode;
        this.name = name;
        this.nodeId = nodeId;
    }

    public BusStop() {

    }

    public static BusStop toEntity(String cityCode, String name, String nodeId) {
        return new BusStop(cityCode, name, nodeId);
    }

    public void update(String cityCode, String name, String nodeId,
        List<Bus> buses) {
        this.cityCode = cityCode;
        this.name = name;
        this.nodeId = nodeId;
        this.busList = buses;
    }
}
