package com.example.busnotice.domain.fcmToken;

import com.example.busnotice.domain.fcmToken.req.CreateFCMTokenRequest;
import com.example.busnotice.domain.schedule.Schedule;
import com.example.busnotice.domain.schedule.ScheduleService;
import com.example.busnotice.domain.schedule.repository.ScheduleRepository;
import com.example.busnotice.domain.schedule.res.ScheduleArrivalResponse;
import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.user.UserRepository;
import com.example.busnotice.global.code.ErrorCode;
import com.example.busnotice.global.exception.FCMTokenException;
import com.example.busnotice.global.exception.UserException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.BatchResponse;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewFCMService {

    private final FCMRepository fcmRepository;
    private final UserRepository userRepository;
    private final ScheduleService scheduleService;
    private final ScheduleRepository scheduleRepository;

    // ObjectMapper는 Bean 주입 받아도 되고, 간단히 새로 생성해도 됩니다.
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ------------------------------------------------------------------
    // FCM 토큰 등록/삭제
    // ------------------------------------------------------------------
    @Transactional
    public void createFCMToken(Long userId, CreateFCMTokenRequest createFCMTokenRequest) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserException(ErrorCode.USER_NOT_FOUND)
        );

        // 동일한 토큰이 다른 유저에 묶여 있는 경우 정리
        List<FCMToken> existingTokens = fcmRepository.findAllByToken(createFCMTokenRequest.token());
        if (!existingTokens.isEmpty()) {
            fcmRepository.deleteAll(existingTokens);
        }

        // 해당 유저에 기존 토큰이 있으면 업데이트
        Optional<FCMToken> optionalFCMToken = fcmRepository.findByUser(user);
        if (optionalFCMToken.isPresent()) {
            optionalFCMToken.get().update(createFCMTokenRequest.token());
            return;
        }

        // 없으면 새로 저장
        fcmRepository.save(createFCMTokenRequest.toEntity(user));
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
//
//        // ------------------------------------------------------------------
//    // 알림 전송 (1분 주기)
//    // ------------------------------------------------------------------
//    @Scheduled(fixedRate = 60000) // 1분
//    @Transactional(readOnly = true)
//    public void sendNotification() throws UnsupportedEncodingException, JsonProcessingException {
//        List<FCMToken> allTokens = fcmRepository.findAll();
//        List<NewUserNotificationData> notifications = new ArrayList<>();
//
//        for (FCMToken token : allTokens) {
//            User user = token.getUser();
//            log.info("푸시 대상 유저: {}", user.getName());
//
//            // 현재 스케줄 도착 정보 조회 (버스/지하철 혼합)
//            ScheduleArrivalResponse scheduleInfo =
//                    scheduleService.현재_스케줄의_교통편_정보(user.getId());
//
//            if (scheduleInfo == null) {
//                log.info("{} → 현재 스케줄 없음", user.getName());
//                continue;
//            }
//
//            // 스케줄 알림 ON 여부 확인
//            Optional<Schedule> schedOpt = scheduleRepository.findById(scheduleInfo.id());
//            if (schedOpt.isEmpty() || !Boolean.TRUE.equals(schedOpt.get().getIsAlarmOn())) {
//                log.info("{} → 현재 스케줄 있지만 스케줄 알림 OFF 상태", user.getName());
//                continue;
//            }
//
//            // ------------------------------
//            // 섹션을 title/detail 문자열로 변환
//            // ------------------------------
//            List<NewUserNotificationData.SimpleSectionDto> simpleSections =
//                    scheduleInfo.sections().stream()
//                            .map(section -> {
//                                // BUS
//                                if ("BUS".equalsIgnoreCase(section.type()) && section.busStop() != null) {
//                                    String title = "[버스] " + safe(section.busStop().busStopName());
//
//                                    String detail = section.busStop().busArrivals().stream()
//                                            .map(b -> {
//                                                String routeNo = safe(b.routeno()); // 503번
//                                                int sec = b.arrtime();              // 초
//                                                String minStr = toMinuteString(sec);
//                                                String hops = b.arrprevstationcnt() + "정거장";
//                                                return routeNo + " (" + minStr + ", " + hops + ")";
//                                            })
//                                            .filter(s -> !s.isBlank())
//                                            .collect(java.util.stream.Collectors.joining(", "));
//
//                                    if (detail.isBlank()) detail = "도착 정보가 없습니다.";
//                                    return new NewUserNotificationData.SimpleSectionDto(title, detail);
//                                }
//
//                                // SUBWAY
//                                if ("SUBWAY".equalsIgnoreCase(section.type()) && section.subway() != null) {
//                                    String lineName = guessLineName(
//                                            section.subway().arrivals().stream()
//                                                    .map(a -> a.subwayId())
//                                                    .filter(s -> s != null && !s.isBlank())
//                                                    .findFirst()
//                                                    .orElse(null)
//                                    );
//
//                                    String updn = section.subway().arrivals().stream()
//                                            .map(a -> a.updnLine())
//                                            .filter(s -> s != null && !s.isBlank())
//                                            .findFirst()
//                                            .orElse("상/하행");
//
//                                    String dirName = section.subway().dirName();
//
//                                    String station = safe(section.subway().stationName());
//                                    String title = "[지하철] " + lineName + " " + station + "역 (" + dirName + ")";
//
//                                    String detail = section.subway().arrivals().stream()
//                                            .map(a -> {
//                                                // barvlDt(초) → "X분" 변환, 파싱 안되면 "도착 예정"
//                                                String eta;
//                                                try {
//                                                    int sec = Integer.parseInt(a.barvlDt());
//                                                    eta = toMinuteString(sec).replace("분 후", "분");
//                                                } catch (Exception e) {
//                                                    eta = "도착 예정";
//                                                }
//                                                String extra = (a.arvlMsg2() != null && !a.arvlMsg2().isBlank())
//                                                        ? " (" + a.arvlMsg2() + ")"
//                                                        : "";
//                                                return eta + extra;
//                                            })
//                                            .filter(s -> !s.isBlank())
//                                            .collect(java.util.stream.Collectors.joining(", "));
//
//                                    if (detail.isBlank()) detail = "도착 정보가 없습니다.";
//                                    return new NewUserNotificationData.SimpleSectionDto(title, detail);
//                                }
//
//                                return null;
//                            })
//                            .filter(Objects::nonNull)
//                            .toList();
//
//            if (simpleSections.isEmpty()) {
//                log.info("{} → 변환 가능한 섹션 없음", user.getName());
//                continue;
//            }
//
//            notifications.add(new NewUserNotificationData(
//                    token.getToken(),
//                    scheduleInfo.id(),
//                    scheduleInfo.name(),
//                    scheduleInfo.days(),
//                    simpleSections
//            ));
//        }
//
//        if (notifications.isEmpty()) {
//            log.info("전송할 알림 없음");
//            return;
//        }
//
//        // ------------------------------
//        // ✅ FCM 전송 (새 DTO 기준 페이로드)
//        // ------------------------------
//        for (NewUserNotificationData n : notifications) {
//            Message message = Message.builder()
//                    .setToken(n.token())
//                    .putData("scheduleId", n.scheduleId().toString())
//                    .putData("scheduleName", n.scheduleName())
//                    .putData("days", n.days().toString())
//                    .putData("sections", objectMapper.writeValueAsString(n.sections()))
//                    .build();
//
//            log.info("푸시 페이로드: scheduleName={}, sections={}", n.scheduleName(), n.sections());
//
//            try {
//                String response = FirebaseMessaging.getInstance().send(message);
//                log.info("FCM 전송 성공: {}", response);
//            } catch (FirebaseMessagingException e) {
//                log.error("FCM 전송 실패: {}", e.getMessage());
//            }
//        }
//    }
    @Scheduled(fixedRate = 60000) // 1분 주기
    @Transactional(readOnly = true)
    public void sendNotification() throws UnsupportedEncodingException, JsonProcessingException {
        List<FCMToken> allTokens = fcmRepository.findAll();
        List<NewUserNotificationData> notifications = new ArrayList<>();

        for (FCMToken token : allTokens) {
            User user = token.getUser();
            ScheduleArrivalResponse scheduleInfo =
                    scheduleService.현재_스케줄의_교통편_정보(user.getId());

            if (scheduleInfo == null) continue;

            Optional<Schedule> schedOpt = scheduleRepository.findById(scheduleInfo.id());
            if (schedOpt.isEmpty() || !Boolean.TRUE.equals(schedOpt.get().getIsAlarmOn())) continue;

            List<NewUserNotificationData.SimpleSectionDto> simpleSections =
                    scheduleInfo.sections().stream()
                            .map(section -> {
                                if ("BUS".equalsIgnoreCase(section.type()) && section.busStop() != null) {
                                    String title = "[버스] " + safe(section.busStop().busStopName());
                                    String detail = section.busStop().busArrivals().stream()
                                            .map(b -> {
                                                String routeNo = safe(b.routeno());
                                                int sec = b.arrtime();
                                                String minStr = toMinuteString(sec);
                                                String hops = b.arrprevstationcnt() + "정거장";
                                                return routeNo + " (" + minStr + ", " + hops + ")";
                                            })
                                            .filter(s -> !s.isBlank())
                                            .collect(Collectors.joining(", "));
                                    if (detail.isBlank()) detail = "도착 정보가 없습니다.";
                                    return new NewUserNotificationData.SimpleSectionDto(title, detail);
                                }

                                if ("SUBWAY".equalsIgnoreCase(section.type()) && section.subway() != null) {
                                    String lineName = guessLineName(
                                            section.subway().arrivals().stream()
                                                    .map(a -> a.subwayId())
                                                    .filter(s -> s != null && !s.isBlank())
                                                    .findFirst().orElse(null)
                                    );
                                    String dirName = section.subway().dirName();
                                    String station = safe(section.subway().stationName());
                                    String title = "[지하철] " + lineName + " " + station + "역 (" + dirName + ")";
                                    String detail = section.subway().arrivals().stream()
                                            .map(a -> {
                                                String eta;
                                                try {
                                                    int sec = Integer.parseInt(a.barvlDt());
                                                    eta = toMinuteString(sec).replace("분 후", "분");
                                                } catch (Exception e) {
                                                    eta = "도착 예정";
                                                }
                                                String extra = (a.arvlMsg2() != null && !a.arvlMsg2().isBlank())
                                                        ? " (" + a.arvlMsg2() + ")"
                                                        : "";
                                                return eta + extra;
                                            })
                                            .filter(s -> !s.isBlank())
                                            .collect(Collectors.joining(", "));
                                    if (detail.isBlank()) detail = "도착 정보가 없습니다.";
                                    return new NewUserNotificationData.SimpleSectionDto(title, detail);
                                }
                                return null;
                            })
                            .filter(Objects::nonNull)
                            .toList();

            if (simpleSections.isEmpty()) continue;

            notifications.add(new NewUserNotificationData(
                    token.getToken(),
                    scheduleInfo.id(),
                    scheduleInfo.name(),
                    scheduleInfo.days(),
                    simpleSections
            ));
        }

        if (notifications.isEmpty()) {
            log.info("전송할 알림 없음");
            return;
        }

        // ✅ 500개 단위로 배치 전송
        List<Message> messages = new ArrayList<>();
        for (NewUserNotificationData n : notifications) {
            Message message = Message.builder()
                    .setToken(n.token())
                    .putData("scheduleId", n.scheduleId().toString())
                    .putData("scheduleName", n.scheduleName())
                    .putData("days", n.days().toString())
                    .putData("sections", objectMapper.writeValueAsString(n.sections()))
                    .build();
            messages.add(message);
        }

        // 500개 단위로 분할하여 배치 전송
        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        for (int i = 0; i < messages.size(); i += 500) {
            int end = Math.min(i + 500, messages.size());
            List<Message> batch = messages.subList(i, end);

            try {
                BatchResponse response = fm.sendAll(batch);
                log.info("✅ 배치 전송 완료: {}개 요청 중 {}개 성공, {}개 실패",
                        batch.size(), response.getSuccessCount(), response.getFailureCount());

                // 실패 로그 출력
                response.getResponses().stream()
                        .filter(r -> !r.isSuccessful())
                        .forEach(r -> log.warn("❌ FCM 전송 실패: {}", r.getException().getMessage()));

            } catch (FirebaseMessagingException e) {
                log.error("❌ FCM 배치 전송 오류: {}", e.getMessage());
            }
        }
    }

    // 테스트용 수동 트리거
    public void sendTestNotification() throws UnsupportedEncodingException, JsonProcessingException {
        sendNotification();
    }

    // ------------------------------------------------------------------
    // 헬퍼
    // ------------------------------------------------------------------
    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    /**
     * 초 → "곧 도착" 또는 "X분 후"
     */
    private static String toMinuteString(int seconds) {
        int min = Math.max(seconds / 60, 0);
        if (min <= 0) return "곧 도착";
        return min + "분 후";
    }

    /**
     * subwayId → 호선명 (서울시 코드 예시)
     * 실제 서비스에서는 코드 테이블로 정확 매핑 권장
     */
    private static String guessLineName(String subwayId) {
        if (subwayId == null || subwayId.isBlank()) return "지하철";
        try {
            int code = Integer.parseInt(subwayId);
            return switch (code) {
                case 1001 -> "1호선";
                case 1002 -> "2호선";
                case 1003 -> "3호선";
                case 1004 -> "4호선";
                case 1005 -> "5호선";
                case 1006 -> "6호선";
                case 1007 -> "7호선";
                case 1008 -> "8호선";
                case 1009 -> "9호선";
                default -> "지하철";
            };
        } catch (NumberFormatException e) {
            return "지하철";
        }
    }
}