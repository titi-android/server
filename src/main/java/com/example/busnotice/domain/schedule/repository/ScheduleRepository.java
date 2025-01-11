package com.example.busnotice.domain.schedule.repository;

import com.example.busnotice.domain.schedule.Schedule;
import com.example.busnotice.domain.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long>,
    ScheduleCustomRepository {

    Optional<List<Schedule>> findAllByUser(User user);

    Optional<Schedule> findByIdAndUserId(Long scheduleId, Long userId);
}
