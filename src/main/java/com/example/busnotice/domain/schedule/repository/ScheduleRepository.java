package com.example.busnotice.domain.schedule.repository;

import com.example.busnotice.domain.schedule.Schedule;
import com.example.busnotice.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long>,
        ScheduleCustomRepository {

    List<Schedule> findAllByUser(User user);

    Optional<Schedule> findByIdAndUserId(Long scheduleId, Long userId);
}
