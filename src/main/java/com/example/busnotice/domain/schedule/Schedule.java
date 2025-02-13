package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.busStop.BusStop;
import com.example.busnotice.domain.user.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalTime;
import java.util.List;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
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

    @Column(columnDefinition = "TIME", nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @Column(columnDefinition = "TIME", nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.REMOVE)
    private List<BusStop> busStops;

    @Embedded
    private DestinationInfo destinationInfo;

    @Column(nullable = false)
    private Boolean isAlarmOn;

    public Schedule(User user, String scheduleName, List<String> daysList,
        LocalTime startTime, LocalTime endTime,
        List<BusStop> busStops, DestinationInfo destinationInfo, Boolean isAlarmOn) {
        this.user = user;
        this.name = scheduleName;
        this.daysList = daysList;
        this.startTime = startTime;
        this.endTime = endTime;
        this.busStops = busStops;
        this.destinationInfo = destinationInfo;
        this.isAlarmOn = isAlarmOn;
    }

    @Embeddable
    @AttributeOverrides({
        @AttributeOverride(name = "regionName", column = @Column(name = "desRegionName")),
        @AttributeOverride(name = "busStopName", column = @Column(name = "desBusStopName")),
        @AttributeOverride(name = "nodeId", column = @Column(name = "desNodeId"))
    })
    public static class DestinationInfo {

        public String regionName;
        public String busStopName;
        public String nodeId;

        public DestinationInfo(String regionName, String busStopName, String nodeId) {
            this.regionName = regionName;
            this.busStopName = busStopName;
            this.nodeId = nodeId;
        }

        public DestinationInfo() {

        }
    }

    public Schedule() {
    }

    public static Schedule toEntity(
        User user,
        String scheduleName,
        List<String> daysList,
        LocalTime startTime,
        LocalTime endTime,
        List<BusStop> busStops,
        DestinationInfo destinationInfo,
        Boolean isAlarmOn
    ) {
        return new Schedule(
            user, scheduleName, daysList, startTime, endTime, busStops, destinationInfo, isAlarmOn
        );
    }

    public void update(String name, List<String> daysList, LocalTime startTime, LocalTime endTime,
        List<BusStop> busStops, DestinationInfo destinationInfo, Boolean isAlarmOn) {
        this.name = name;
        this.daysList = daysList;
        this.startTime = startTime;
        this.endTime = endTime;
        this.busStops = busStops;
        this.destinationInfo = destinationInfo;
        this.isAlarmOn = isAlarmOn;
    }

    public boolean updateAlarm() {
        System.out.println("기존 알림 상태 = " + this.isAlarmOn);
        this.isAlarmOn = !this.isAlarmOn;
        System.out.println("수정된 알림 상태 = " + this.isAlarmOn);
        return this.isAlarmOn;
    }
}
