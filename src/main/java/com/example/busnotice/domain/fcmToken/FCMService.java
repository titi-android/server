package com.example.busnotice.domain.fcmToken;

import com.example.busnotice.domain.fcmToken.req.CreateFCMTokenRequest;
import com.example.busnotice.domain.schedule.Schedule;
import com.example.busnotice.domain.schedule.ScheduleService;
import com.example.busnotice.domain.schedule.repository.ScheduleRepository;
import com.example.busnotice.domain.schedule.res.ScheduleResponses;
import com.example.busnotice.domain.schedule.res.ScheduleResponses.BusInfoDto;
import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.user.UserRepository;
import com.example.busnotice.global.code.StatusCode;
import com.example.busnotice.global.exception.UserException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {


    private final FCMRepository fcmRepository;
    private final UserRepository userRepository;
    private final ScheduleService scheduleService;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public void createFCMToken(Long userId, CreateFCMTokenRequest createFCMTokenRequest) {
        User user = userRepository.findById(userId).orElseThrow(
            () -> new UserException(StatusCode.NOT_FOUND, "해당 ID 의 유저가 존재하지 않습니다.")
        );

        Optional<FCMToken> optionalFCMToken = fcmRepository.findByUser(user);
        // 기존 토큰 존재하는 경우 업데이트
        if (optionalFCMToken.isPresent()) {
            FCMToken fcmToken = optionalFCMToken.get();
            fcmToken.update(createFCMTokenRequest.token());
            return;
        }
        // 그렇지 않은 경우 새로 생성
        fcmRepository.save(createFCMTokenRequest.toEntity(user));
    }

    @Scheduled(fixedRate = 60000)  // 1분(60,000ms)마다 실행
    @Transactional(readOnly = true)  // 트랜잭션 적용
    public void sendNotification() throws UnsupportedEncodingException {
        List<FCMToken> allTokens = fcmRepository.findAll();
        List<UserNotificationData> notifications = new ArrayList<>();

        for (FCMToken token : allTokens) {
            log.info("토큰의 유저 이름: {}", token.getUser().getName());
            ScheduleResponses sr = scheduleService.현재_스케줄의_가장_빨리_도착하는_첫번째_두번째_버스_정보(
                token.getUser().getId());
            if (sr != null) {
                Schedule schedule = scheduleRepository.findById(sr.id()).get();
                if (!schedule.getIsAlarmOn()) {
                    log.info("{} 의 현재 스케줄이 존재하나, 알림 전송 대상이 아닙니다. : {}", token.getUser().getName(),
                        sr);
                    continue;
                } else if (sr.busInfos().isEmpty()) {
                    log.info("{} 의 현재 스케줄이 존재하나, 도착 예정인 버스가 없습니다. : {}", token.getUser().getName(),
                        sr);
                } else {
                    log.info("{} 의 현재 스케줄이 존재하며, 도착 예정인 버스가 존재합니다. : {}", token.getUser().getName(),
                        sr);
                }
                BusInfoDto fb = sr.busInfos().size() > 0 ? sr.busInfos().get(0)
                    : new BusInfoDto(0, 0, "", "", "", "", "", "");
                BusInfoDto sb = sr.busInfos().size() > 1 ? sr.busInfos().get(1)
                    : new BusInfoDto(0, 0, "", "", "", "", "", "");

                notifications.add(new UserNotificationData(
                    token.getToken(),
                    sr.name(),
                    sr.days(),
                    sr.busStopName(),
                    fb.routeno(), fb.arrprevstationcnt(), fb.arrtime(),
                    sb.routeno(), sb.arrprevstationcnt(), sb.arrtime()
                ));
            } else {
                log.info("{} 의 현재 스케줄이 존재하지 않습니다.", token.getUser().getName());
            }
        }

        if (notifications.isEmpty()) {
            log.warn("현재 시간대의 스케줄을 보유한 유저가 존재하지 않습니다. 알림 전송을 중단합니다..");
            return;
        }

        for (UserNotificationData notification : notifications) {
            Message message = Message.builder()
                .setToken(notification.token())
                .putData("scheduleName", notification.scheduleName())
                .putData("days", notification.days())
                .putData("busStopName", notification.busStopName())
                .putData("firstBusName", notification.firstBusName())
                .putData("firstArrPrevStCnt", String.valueOf(notification.firstArrPrevStCnt()))
                .putData("firstArrTime", String.valueOf(notification.firstArrTime()))
                .putData("secondBusName", notification.secondBusName())
                .putData("secondArrPrevStCnt", String.valueOf(notification.secondArrPrevStCnt()))
                .putData("secondArrTime", String.valueOf(notification.secondArrTime()))
                .build();

            try {
                String response = FirebaseMessaging.getInstance().send(message);
                log.info("메시지 전송 성공: {}, 토큰: {}", response, notification.token());
            } catch (FirebaseMessagingException e) {
                log.error("메시지 전송 실패: {}, 토큰: {}", e.getMessage(), notification.token());
            }
        }
    }


}
