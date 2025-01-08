package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.user.User;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findAllByUser(User user);

    @Query("SELECT s FROM Schedule s " +
        "WHERE s.user = :user " + // 유저가 일치
        "AND :currentTime BETWEEN s.startTime AND s.endTime " + // 현재시간이 스케줄 내 시간에 포함
        "AND s.days = :today") // 요일(한글) 일치
    Schedule findByCurrentTimeAndDay(
        @Param("user") User user,
        @Param("today") String today,
        @Param("currentTime") LocalTime now
    );

}
