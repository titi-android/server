package com.example.busnotice.domain.schedule;

import com.example.busnotice.domain.user.User;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findAllByUserName(String username);

    List<Schedule> findAllByUser(User user);

    @Query("SELECT s FROM Schedule s WHERE s.user = :user AND :currentTime BETWEEN s.startTime AND s.endTime")
    Schedule findByCurrentTime(@Param("user") User user, @Param("currentTime") LocalTime now);

}
