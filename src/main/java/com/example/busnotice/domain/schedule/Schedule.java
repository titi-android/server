package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.busStop.BusStop;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private String name;

    @Column
    LocalDateTime startTime;

    @Column
    LocalDateTime endTime;

    @OneToOne
    private BusStop busStop;

    public Schedule(User user, String scheduleName, BusStop busStop, LocalDateTime startTime,
        LocalDateTime endTime) {
    }

    public Schedule() {
    }

    public static Schedule toEntity(
        User user,
        String scheduleName,
        BusStop busStop,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        return new Schedule(
            user, scheduleName, busStop, startTime, endTime
        );
    }
}
