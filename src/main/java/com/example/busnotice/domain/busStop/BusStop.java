package com.example.busnotice.domain.busStop;

import com.example.busnotice.domain.bus.Bus;
import com.example.busnotice.domain.schedule.Schedule;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.List;
import lombok.Builder;
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
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @Column
    private String name;

    @OneToMany(mappedBy = "busStop")
    private List<Bus> busList;

    public BusStop(String name) {
    }

    public BusStop() {

    }

    public static BusStop toEntity(String name){
        return new BusStop(name);
    }
}
