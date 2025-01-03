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
    LocalDateTime startTime;

    @Column(nullable = false)
    LocalDateTime endTime;

    @OneToOne
    private BusStop busStop;

    public Schedule(User user, String scheduleName,  LocalDateTime startTime,
        LocalDateTime endTime, BusStop busStop) {
        this.user = user;
        this.name = scheduleName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.busStop = busStop;
    }

    public Schedule() {
    }

    public static Schedule toEntity(
        User user,
        String scheduleName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BusStop busStop
    ) {
        return new Schedule(
            user, scheduleName, startTime, endTime, busStop
        );
    }

}
