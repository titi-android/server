package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.busStop.BusStop;
import com.example.busnotice.domain.user.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.LocalTime;
import java.util.List;
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

    @ElementCollection
    @CollectionTable(name = "schedule_days", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(nullable = false)
    private List<String> daysList; // 요일 리스트

    @Column(nullable = false)
    private String regionName; // 지역 이름

    @Column(columnDefinition = "TIME", nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @Column(columnDefinition = "TIME", nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @OneToOne(cascade = CascadeType.REMOVE)
    private BusStop busStop;

    @Column(nullable = false)
    private Boolean isAlarmOn;

    public Schedule(User user, String scheduleName, List<String> daysList, String regionName,
        LocalTime startTime,
        LocalTime endTime, BusStop busStop) {
        this.user = user;
        this.name = scheduleName;
        this.daysList = daysList;
        this.regionName = regionName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.busStop = busStop;
        this.isAlarmOn = true;
    }

    public Schedule() {
    }

    public static Schedule toEntity(
        User user,
        String scheduleName,
        List<String> daysList,
        String regionName,
        LocalTime startTime,
        LocalTime endTime,
        BusStop busStop
    ) {
        return new Schedule(
            user, scheduleName, daysList, regionName, startTime, endTime, busStop
        );
    }

    public void update(String name, List<String> daysList, LocalTime startTime, LocalTime endTime,
        BusStop busStop) {
        this.name = name;
        this.daysList = daysList;
        this.startTime = startTime;
        this.endTime = endTime;
        this.busStop = busStop;
    }

    public boolean updateAlarm() {
        this.isAlarmOn = !this.isAlarmOn;
        return this.isAlarmOn;
    }
}
