package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.busStop.BusStop;
import com.example.busnotice.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String days; // 요일

    @Column(nullable = false)
    LocalTime startTime;

    @Column(nullable = false)
    LocalTime endTime;

    @OneToOne
    private BusStop busStop;

    public Schedule(User user, String scheduleName, String days, LocalTime startTime,
        LocalTime endTime, BusStop busStop) {
        this.user = user;
        this.name = scheduleName;
        this.days = days;
        this.startTime = startTime;
        this.endTime = endTime;
        this.busStop = busStop;
    }

    public Schedule() {
    }

    public static Schedule toEntity(
        User user,
        String scheduleName,
        String days,
        LocalTime startTime,
        LocalTime endTime,
        BusStop busStop
    ) {
        return new Schedule(
            user, scheduleName, days, startTime, endTime, busStop
        );
    }

}
