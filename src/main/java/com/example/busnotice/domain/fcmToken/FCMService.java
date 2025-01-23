package com.example.busnotice.domain.fcmToken;

import com.example.busnotice.domain.fcmToken.req.CreateFCMTokenRequest;
import com.example.busnotice.domain.schedule.ScheduleService;
import com.example.busnotice.domain.schedule.res.ScheduleResponses;
import com.example.busnotice.domain.schedule.res.ScheduleResponses.BusInfoDto;
import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.user.UserRepository;
import com.example.busnotice.global.code.StatusCode;
import com.example.busnotice.global.exception.UserException;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.SendResponse;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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

//    @Scheduled(fixedRate = 60000)  // 1분(60,000ms)마다 실행
//    @Transactional(readOnly = true)  // 트랜잭션 적용
    public void sendNotification() throws UnsupportedEncodingException {
        List<FCMToken> allTokens = fcmRepository.findAll();
        List<UserNotificationData> notifications = new ArrayList<>();

        for (FCMToken token : allTokens) {
            log.info("토큰의 유저 이름: {}", token.getUser().getName());
            ScheduleResponses sr = scheduleService.현재_스케줄의_가장_빨리_도착하는_첫번째_두번째_버스_정보(
                token.getUser().getId());

            if (sr != null) {
                log.info("{} 의 현재 스케줄이 존재합니다: {}", token.getUser().getName(), sr);
                BusInfoDto fb = sr.busInfos().size() > 0 ? sr.busInfos().get(0) : new BusInfoDto(0, 0, "", "", "", "", "", "");
                BusInfoDto sb = sr.busInfos().size() > 1 ? sr.busInfos().get(1) : new BusInfoDto(0, 0, "", "", "", "", "", "");

                notifications.add(new UserNotificationData(
                    token.getToken(),
                    sr.name(),
                    sr.days(),
                    sr.busStopName(),
                    fb.routeno(), fb.arrprevstationcnt(), fb.arrprevstationcnt(),
                    sb.routeno(), sb.arrprevstationcnt(), sb.arrprevstationcnt()));
            }
            else{
                log.info("{} 의 현재 스케줄이 존재하지 않습니다.", token.getUser().getName());
            }
        }

        if (notifications.isEmpty()) {
            log.warn("보낼 FCM 토큰이 없습니다. 알림 전송을 중단합니다.");
            return;
        }

        Map<String, Message> tokenToMessageMap = notifications.stream()
            .collect(Collectors.toMap(
                UserNotificationData::token,
                notification -> Message.builder()
                    .setToken(notification.token())
                    .putData("scheduleName", notification.scheduleName()) // 스케줄 이름
                    .putData("days", notification.days()) // 요일 ex) 월요일
                    .putData("busStopName", notification.busStopName()) // 버스정류장 이름
                    .putData("firstBusName", notification.firstBusName()) // 첫번째 도착 예정 버스 이름(없으면 "")
                    .putData("firstArrPrevStCnt", String.valueOf(notification.firstArrPrevStCnt()))  // 잔여 정류장 수
                    .putData("firstArrTime", String.valueOf(notification.firstArrTime())) // 예정 도착 소요 시간(초 단위)
                    .putData("secondBusName", notification.secondBusName()) // 두번째 도착 예정 버스 이름(없으면 "")
                    .putData("secondArrPrevStCnt", String.valueOf(notification.secondArrPrevStCnt())) // 잔여 정류장 수
                    .putData("secondArrTime", String.valueOf(notification.secondArrTime())) // 예정 도착 소요 시간(초 단위)
                    .build(),
                (existing, replacement) -> existing
            ));

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendAll(new ArrayList<>(tokenToMessageMap.values()));

            List<String> successfulTokens = new ArrayList<>();
            List<String> failedTokens = new ArrayList<>();
            List<String> tokenList = new ArrayList<>(tokenToMessageMap.keySet());

            for (int i = 0; i < response.getResponses().size(); i++) {
                SendResponse sendResponse = response.getResponses().get(i);
                if (sendResponse.isSuccessful()) {
                    successfulTokens.add(tokenList.get(i));
                } else {
                    failedTokens.add(tokenList.get(i) + " - " + sendResponse.getException().getMessage());
                }
            }

            log.info("메시지 전송 성공: {}개", successfulTokens.size());
            if (!failedTokens.isEmpty()) {
                log.error("메시지 전송 실패: {}", String.join(", ", failedTokens));
            }
        } catch (FirebaseMessagingException e) {
            log.error("FCM 메시지 전송 실패 - 전체 실패 발생", e);
        }
    }


}
