package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.user.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalTime;
import java.util.List;

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
    private List<String> daysList;

    @Column(columnDefinition = "TIME", nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @Column(columnDefinition = "TIME", nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Section> sections;

    @Embedded
    private DestinationInfo destinationInfo;

    @Column(nullable = false)
    private Boolean isAlarmOn;

    public Schedule() {
    }

    public Schedule(User user, String name, List<String> daysList, LocalTime startTime, LocalTime endTime,
                    List<Section> sections, DestinationInfo destinationInfo, Boolean isAlarmOn) {
        this.user = user;
        this.name = name;
        this.daysList = daysList;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sections = sections;
        this.destinationInfo = destinationInfo;
        this.isAlarmOn = isAlarmOn;
    }

    public boolean updateAlarmStatus() {
        this.isAlarmOn = !isAlarmOn;
        return this.isAlarmOn;
    }

    @Embeddable
    @AttributeOverrides({
            @AttributeOverride(name = "regionName", column = @Column(name = "desRegionName")),
            @AttributeOverride(name = "busStopName", column = @Column(name = "desBusStopName")),
            @AttributeOverride(name = "nodeId", column = @Column(name = "desNodeId"))
    })
    @Data
    public static class DestinationInfo {
        private String type;
        private String regionName;
        private String placeName;
        private String nodeId;

        public DestinationInfo() {
        }

        public DestinationInfo(String type, String regionName, String busStopName, String nodeId) {
            this.type = type;
            this.regionName = regionName;
            this.placeName = busStopName;
            this.nodeId = nodeId;
        }
    }
}