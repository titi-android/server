package com.example.busnotice.domain.schedule.repository;

import com.example.busnotice.domain.schedule.QSchedule;
import com.example.busnotice.domain.schedule.Schedule;
import com.example.busnotice.domain.user.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScheduleCustomRepositoryImpl implements ScheduleCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final QSchedule schedule = QSchedule.schedule;

    @Override
    public Optional<Schedule> findByCurrentTimeAndDay(User user, String today, LocalTime now) {
        return Optional.ofNullable(jpaQueryFactory
            .selectFrom(schedule)
            .where(
                schedule.user.eq(user),
                schedule.startTime.loe(now).and(schedule.endTime.goe(now)),
                schedule.days.eq(today)
            )
            .fetchOne());
    }

    @Override
    public List<Schedule> findAllByUserAndDays(User user, String today) {
        List<Schedule> schedules = jpaQueryFactory
            .selectFrom(schedule)
            .where(
                schedule.user.eq(user),
                schedule.days.eq(today)
            )
            .fetch();
        return schedules;
    }
}
