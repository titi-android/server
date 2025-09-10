package com.example.busnotice.domain.schedule.repository;

import com.example.busnotice.domain.schedule.Schedule;
import com.example.busnotice.domain.user.User;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleCustomRepository {

    Optional<Schedule> findByCurrentTimeAndDay(User user, String today, LocalTime now);

    List<Schedule> findAllByUserAndDaysOrderByStartTime(User user, String today);
}
