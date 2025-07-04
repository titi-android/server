package com.example.busnotice.domain.fcmToken;

import com.example.busnotice.domain.fcmToken.req.CreateFCMTokenRequest;
import com.example.busnotice.domain.schedule.Schedule;
import com.example.busnotice.domain.schedule.ScheduleService;
import com.example.busnotice.domain.schedule.repository.ScheduleRepository;
import com.example.busnotice.domain.schedule.res.ScheduleResponse;
import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.user.UserRepository;
import com.example.busnotice.global.code.ErrorCode;
import com.example.busnotice.global.exception.FCMTokenException;
import com.example.busnotice.global.exception.UserException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {


    private final FCMRepository fcmRepository;
    private final UserRepository userRepository;
    private final ScheduleService scheduleService;
    private final ScheduleRepository scheduleRepository;
    ObjectMapper objectMapper = new ObjectMapper();  // JSON 변환기

    @Transactional
    public void createFCMToken(Long userId, CreateFCMTokenRequest createFCMTokenRequest) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserException(ErrorCode.USER_NOT_FOUND)
        );

        // 동일한 토큰이 존재하는 모든 FCMToken 삭제 -> 다른 유저가 동일한 FCMToken 을 가지고 있는 경우를 방지하기 위해
        List<FCMToken> existingTokens = fcmRepository.findAllByToken(createFCMTokenRequest.token());
        if (!existingTokens.isEmpty()) {
            fcmRepository.deleteAll(existingTokens);
        }

        // 기존 유저의 토큰이 있는 경우 업데이트
        Optional<FCMToken> optionalFCMToken = fcmRepository.findByUser(user);
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
    public void sendNotification() throws UnsupportedEncodingException, JsonProcessingException {
        List<FCMToken> allTokens = fcmRepository.findAll();
        List<UserNotificationData> notifications = new ArrayList<>();

        for (FCMToken token : allTokens) {
            log.info("토큰의 유저 이름: {}", token.getUser().getName());
            ScheduleResponse sr = scheduleService.현재_스케줄의_가장_빨리_도착하는_첫번째_두번째_버스_정보(
                    token.getUser().getId());
            if (sr != null) {
                Schedule schedule = scheduleRepository.findById(sr.id()).get();
                if (!schedule.getIsAlarmOn()) {
                    log.info("{} 의 현재 스케줄이 존재하나, 알림 전송 대상이 아닙니다. : {}", token.getUser().getName(),
                            sr);
                    continue;
                }
                // 정류장 및 도착 예정 버스 정보를 변환
                List<UserNotificationData.BusStopArrInfoDto> busStopInfoDtos = sr.busStopInfos()
                        .stream()
                        .map(busStop -> new UserNotificationData.BusStopArrInfoDto(
                                busStop.busStopName(),
                                busStop.busInfos().stream()
                                        .map(bus -> new UserNotificationData.BusStopArrInfoDto.BusArrInfoDto(
                                                bus.arrprevstationcnt(),
                                                bus.arrtime(),
                                                bus.nodeid(),
                                                bus.nodenm(),
                                                bus.routeid(),
                                                bus.routeno(),
                                                bus.routetp(),
                                                bus.vehicletp()
                                        ))
                                        .collect(Collectors.toList())
                        ))
                        .collect(Collectors.toList());

                notifications.add(new UserNotificationData(
                        token.getToken(),
                        sr.id(),
                        sr.name(),
                        sr.days(),
                        busStopInfoDtos
                ));
            } else {
                log.info("{} 의 현재 스케줄이 존재하지 않습니다.", token.getUser().getName());
            }
        }

        if (notifications.isEmpty()) {
            log.warn("현재 시간대의 스케줄을 보유한 유저가 한명도 존재하지 않습니다. 알림 전송을 중단합니다..");
            return;
        }

        for (UserNotificationData notification : notifications) {
            Message message = Message.builder()
                    .setToken(notification.token())
                    .putData("scheduleId", notification.scheduleId().toString())
                    .putData("scheduleName", notification.scheduleName())
                    .putData("days", notification.days().toString())
                    .putData("busStopInfos",
                            objectMapper.writeValueAsString(notification.busStopInfos()))
                    .build();

            try {
                String response = FirebaseMessaging.getInstance().send(message);
                log.info("메시지 전송 성공: {}, 토큰: {}", response, notification.token());
            } catch (FirebaseMessagingException e) {
                log.error("메시지 전송 실패: {}, 토큰: {}", e.getMessage(), notification.token());
            }
        }
    }

    public void sendTestNotification()
            throws UnsupportedEncodingException, JsonProcessingException {
        sendNotification();
    }

    @Transactional
    public void deleteFCMToken(Long userId) {
        Optional<FCMToken> fcmToken = fcmRepository.findByUserId(userId);
        if (fcmToken.isPresent()) {
            fcmRepository.delete(fcmToken.get());
            return;
        }
        throw new FCMTokenException(ErrorCode.FCM_TOKEN_NOT_FOUND);
    }
}
