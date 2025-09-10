package com.example.busnotice.runner;

import com.example.busnotice.domain.bus.Bus;
import com.example.busnotice.domain.busStop.BusStopSection;
import com.example.busnotice.domain.schedule.Schedule;
import com.example.busnotice.domain.schedule.repository.ScheduleRepository;
import com.example.busnotice.domain.section.Section;
import com.example.busnotice.domain.subway.SubwaySection;
import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;

/*
SELECT s.*
FROM schedule s IGNORE INDEX (idx_schedule_user_start) <- 이거 땠다 붙였다하면서 성능 테스트하면 됨
WHERE s.user_id = 302
  AND EXISTS (
      SELECT 1
      FROM schedule_days d
      WHERE d.schedule_id = s.id
        AND d.days_list = '일요일'
  )
ORDER BY s.start_time ASC;
 */

// 데이터 삽입 예제(users, schedules 수를 조정하면 나머지것들 내부 코드 로직에 의해 다 조절됨, 아래 사용 갯수 적절한듯)
// ./gradlew bootRun --args="--spring.profiles.active=seed --seed.users=30000 --seed.schedulesPerUser=6"

@Component
@Profile("seed") // 실행 시 --spring.profiles.active=seed
@RequiredArgsConstructor
public class PerfSeedRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final PasswordEncoder passwordEncoder;

    // 실행 시 오버라이드 가능: --seed.users=10000 --seed.schedulesPerUser=8
    @Value("${seed.users:5000}")
    private int users;

    @Value("${seed.schedulesPerUser:5}")
    private int schedulesPerUser;

    @Value("${seed.batchSize:1000}")
    private int batchSize;

    private static final List<String> DAYS = List.of("월요일","화요일","수요일","목요일","금요일","토요일","일요일");

    @Override
    @Transactional
    public void run(String... args) {
        // 이미 시드한 적 있으면 스킵(선택)
        if (userRepository.existsByName("perf_user_0")) return;

        final String encoded = passwordEncoder.encode("seedpw");
        final Random rnd = new Random(42);

        List<User> userBatch = new ArrayList<>(batchSize);
        List<Schedule> scheduleBatch = new ArrayList<>(batchSize * schedulesPerUser);

        for (int u = 0; u < users; u++) {
            User user = new User("perf_user_" + u, encoded);
            userBatch.add(user);

            if (userBatch.size() == batchSize) {
                userRepository.saveAll(userBatch);
                // 유저 저장 후 스케줄 생성(외래키 필요)
                attachSchedulesForUsers(userBatch, scheduleBatch, rnd);
                userBatch.clear();
            }
        }
        if (!userBatch.isEmpty()) {
            userRepository.saveAll(userBatch);
            attachSchedulesForUsers(userBatch, scheduleBatch, rnd);
            userBatch.clear();
        }

        if (!scheduleBatch.isEmpty()) {
            scheduleRepository.saveAll(scheduleBatch);
            scheduleBatch.clear();
        }
    }

    private void attachSchedulesForUsers(List<User> users, List<Schedule> scheduleBatch, Random rnd) {
        for (User user : users) {
            for (int i = 0; i < schedulesPerUser; i++) {
                int startMin = 5 * 60 + rnd.nextInt((22 - 5) * 60);
                LocalTime start = LocalTime.of(startMin / 60, startMin % 60);
                int duration = 30 + rnd.nextInt(90);
                LocalTime end = start.plusMinutes(duration);

                int dayCount = 1 + rnd.nextInt(3);
                Set<String> daySet = new LinkedHashSet<>();
                while (daySet.size() < dayCount) {
                    daySet.add(DAYS.get(rnd.nextInt(DAYS.size())));
                }
                List<String> daysList = new ArrayList<>(daySet);

                // 목적지 정보 (랜덤하게 BUS or SUBWAY)
                Schedule.DestinationInfo dest;
                if (rnd.nextBoolean()) {
                    dest = new Schedule.DestinationInfo(
                            "BUS", "SEOUL", "광화문"
                    );
                } else {
                    dest = new Schedule.DestinationInfo(
                            "SUBWAY", "SEOUL", "강남역", "2호선", "삼성역 방면", "DOWN"
                    );
                }

                Schedule schedule = new Schedule(
                        user,
                        "schedule_" + i,
                        daysList,
                        start,
                        end,
                        new ArrayList<>(), // sections
                        dest,
                        rnd.nextBoolean()
                );

                // 🔹 RouteInfo 더미 추가 (랜덤 버스/지하철)
                List<Section> sectionEntities = new ArrayList<>();
                if ("BUS".equalsIgnoreCase(dest.getType())) {
                    BusStopSection busStopSection = new BusStopSection(
                            "SEOUL",
                            "광화문 버스정류장",
                            "node-" + rnd.nextInt(1000),
                            new ArrayList<>()
                    );
                    Bus bus1 = new Bus(busStopSection, "100", "간선");
                    Bus bus2 = new Bus(busStopSection, "740", "간선");
                    busStopSection.setBusList(List.of(bus1, bus2));

                    sectionEntities.add(Section.busSection(0, schedule, busStopSection));
                } else {
                    SubwaySection subwaySection = new SubwaySection(
                            "SEOUL", "2호선", "강남역", "삼성역 방면", "DOWN"
                    );
                    sectionEntities.add(Section.subwaySection(0, schedule, subwaySection));
                }

                schedule.setSections(sectionEntities);
                scheduleBatch.add(schedule);

                if (scheduleBatch.size() >= users.size() * schedulesPerUser
                        || scheduleBatch.size() >= batchSize * schedulesPerUser) {
                    scheduleRepository.saveAll(scheduleBatch);
                    scheduleBatch.clear();
                }
            }
        }
    }
}