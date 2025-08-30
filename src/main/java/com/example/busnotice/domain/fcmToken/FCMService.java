//package com.example.busnotice.domain.fcmToken;
//
//import com.example.busnotice.domain.fcmToken.req.CreateFCMTokenRequest;
//import com.example.busnotice.domain.schedule.Schedule;
//import com.example.busnotice.domain.schedule.ScheduleService;
//import com.example.busnotice.domain.schedule.repository.ScheduleRepository;
//import com.example.busnotice.domain.schedule.res.ScheduleArrivalResponse;
//import com.example.busnotice.domain.user.User;
//import com.example.busnotice.domain.user.UserRepository;
//import com.example.busnotice.global.code.ErrorCode;
//import com.example.busnotice.global.exception.FCMTokenException;
//import com.example.busnotice.global.exception.UserException;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.firebase.messaging.FirebaseMessaging;
//import com.google.firebase.messaging.FirebaseMessagingException;
//import com.google.firebase.messaging.Message;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//import java.util.Optional;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class FCMService {
//
//
//    private final FCMRepository fcmRepository;
//    private final UserRepository userRepository;
//    private final ScheduleService scheduleService;
//    private final ScheduleRepository scheduleRepository;
//    ObjectMapper objectMapper = new ObjectMapper();  // JSON 변환기
//
//    @Transactional
//    public void createFCMToken(Long userId, CreateFCMTokenRequest createFCMTokenRequest) {
//        User user = userRepository.findById(userId).orElseThrow(
//                () -> new UserException(ErrorCode.USER_NOT_FOUND)
//        );
//
//        // 동일한 토큰이 존재하는 모든 FCMToken 삭제 -> 다른 유저가 동일한 FCMToken 을 가지고 있는 경우를 방지하기 위해
//        List<FCMToken> existingTokens = fcmRepository.findAllByToken(createFCMTokenRequest.token());
//        if (!existingTokens.isEmpty()) {
//            fcmRepository.deleteAll(existingTokens);
//        }
//
//        // 기존 유저의 토큰이 있는 경우 업데이트
//        Optional<FCMToken> optionalFCMToken = fcmRepository.findByUser(user);
//        if (optionalFCMToken.isPresent()) {
//            FCMToken fcmToken = optionalFCMToken.get();
//            fcmToken.update(createFCMTokenRequest.token());
//            return;
//        }
//
//        // 그렇지 않은 경우 새로 생성
//        fcmRepository.save(createFCMTokenRequest.toEntity(user));
//    }
//
//    @Scheduled(fixedRate = 60000) // 1분마다 실행
//    @Transactional(readOnly = true)
//    public void sendNotification() throws UnsupportedEncodingException, JsonProcessingException {
//        List<FCMToken> allTokens = fcmRepository.findAll();
//        List<UserNotificationData> notifications = new ArrayList<>();
//
//        for (FCMToken token : allTokens) {
//            User user = token.getUser();
//            log.info("푸시 대상 유저: {}", user.getName());
//
//            // 현재 스케줄 도착 정보 (버스 + 지하철)
//            ScheduleArrivalResponse scheduleInfo =
//                    scheduleService.현재_스케줄의_교통편_정보(user.getId());
//
//            if (scheduleInfo == null) {
//                log.info("{} → 현재 스케줄 없음", user.getName());
//                continue;
//            }
//
//            // 스케줄 자체의 알림 설정 확인
//            Optional<Schedule> schedOpt = scheduleRepository.findById(scheduleInfo.id());
//            if (schedOpt.isEmpty() || !Boolean.TRUE.equals(schedOpt.get().getIsAlarmOn())) {
//                log.info("{} → 현재 스케줄 있지만 스케줄 알림 OFF 상태", user.getName());
//                continue;
//            }
//
//            // section -> 타입별 전용 DTO 변환
//            List<NewUserNotificationData.SimpleSectionDto> sectionDtos = scheduleInfo.sections().stream()
//                    .map(section -> {
//                        if ("BUS".equalsIgnoreCase(section.type()) && section.busStop() != null) {
//                            List<UserNotificationData.BusArrInfoDto> buses =
//                                    section.busStop().busArrivals().stream()
//                                            .map(b -> new UserNotificationData.BusArrInfoDto(
//                                                    b.arrprevstationcnt(),
//                                                    b.arrtime(),
//                                                    b.nodeid(),
//                                                    b.nodenm(),
//                                                    b.routeid(),
//                                                    b.routeno(),
//                                                    b.routetp(),
//                                                    b.vehicletp()
//                                            ))
//                                            .toList();
//                            return new UserNotificationData.BusSectionInfoDto(
//                                    section.busStop().busStopName(),
//                                    buses
//                            );
//                        } else if ("SUBWAY".equalsIgnoreCase(section.type()) && section.subway() != null) {
//                            List<UserNotificationData.SubwayArrInfoDto> trains =
//                                    section.subway().arrivals().stream()
//                                            .map(a -> new UserNotificationData.SubwayArrInfoDto(
//                                                    a.subwayId(),
//                                                    a.updnLine(),
//                                                    a.statnNm(),
//                                                    a.barvlDt(),
//                                                    a.arvlMsg2(),
//                                                    a.arvlCd()
//                                            ))
//                                            .toList();
//                            return new UserNotificationData.SubwaySectionInfoDto(
//                                    section.subway().stationName(),
//                                    trains
//                            );
//                        }
//                        return null;
//                    })
//                    .filter(Objects::nonNull)
//                    .toList();
//
//            notifications.add(new UserNotificationData(
//                    token.getToken(),
//                    scheduleInfo.id(),
//                    scheduleInfo.name(),
//                    scheduleInfo.days(),
//                    sectionDtos
//            ));
//        }
//
//        if (notifications.isEmpty()) {
//            log.info("전송할 알림 없음");
//            return;
//        }
//
//        // FCM 메시지 전송
//        for (UserNotificationData notification : notifications) {
//            Message message = Message.builder()
//                    .setToken(notification.token())
//                    .putData("scheduleId", notification.scheduleId().toString())
//                    .putData("scheduleName", notification.scheduleName())
//                    .putData("days", notification.days().toString())
//                    .putData("sections", objectMapper.writeValueAsString(notification.sections()))
//                    .build();
//            log.info("{}", notification);
//            try {
//                String response = FirebaseMessaging.getInstance().send(message);
//                log.info("FCM 전송 성공: {}", response);
//            } catch (FirebaseMessagingException e) {
//                log.error("FCM 전송 실패: {}", e.getMessage());
//            }
//        }
//    }
//
//    @Scheduled(fixedRate = 60000) // 1분마다 실행
//    @Transactional(readOnly = true)
//    public void newSendNotification() throws UnsupportedEncodingException, JsonProcessingException {
//        List<FCMToken> allTokens = fcmRepository.findAll();
//        List<UserNotificationData> notifications = new ArrayList<>();
//
//        for (FCMToken token : allTokens) {
//            User user = token.getUser();
//            log.info("푸시 대상 유저: {}", user.getName());
//
//            // 현재 스케줄 도착 정보 (버스 + 지하철)
//            ScheduleArrivalResponse scheduleInfo =
//                    scheduleService.현재_스케줄의_교통편_정보(user.getId());
//
//            if (scheduleInfo == null) {
//                log.info("{} → 현재 스케줄 없음", user.getName());
//                continue;
//            }
//
//            // 스케줄 자체의 알림 설정 확인
//            Optional<Schedule> schedOpt = scheduleRepository.findById(scheduleInfo.id());
//            if (schedOpt.isEmpty() || !Boolean.TRUE.equals(schedOpt.get().getIsAlarmOn())) {
//                log.info("{} → 현재 스케줄 있지만 스케줄 알림 OFF 상태", user.getName());
//                continue;
//            }
//
//            // section -> 타입별 전용 DTO 변환
//            List<UserNotificationData.SectionInfoDto> sectionDtos = scheduleInfo.sections().stream()
//                    .map(section -> {
//                        if ("BUS".equalsIgnoreCase(section.type()) && section.busStop() != null) {
//                            List<UserNotificationData.BusArrInfoDto> buses =
//                                    section.busStop().busArrivals().stream()
//                                            .map(b -> new UserNotificationData.BusArrInfoDto(
//                                                    b.arrprevstationcnt(),
//                                                    b.arrtime(),
//                                                    b.nodeid(),
//                                                    b.nodenm(),
//                                                    b.routeid(),
//                                                    b.routeno(),
//                                                    b.routetp(),
//                                                    b.vehicletp()
//                                            ))
//                                            .toList();
//                            return new UserNotificationData.BusSectionInfoDto(
//                                    section.busStop().busStopName(),
//                                    buses
//                            );
//                        } else if ("SUBWAY".equalsIgnoreCase(section.type()) && section.subway() != null) {
//                            List<UserNotificationData.SubwayArrInfoDto> trains =
//                                    section.subway().arrivals().stream()
//                                            .map(a -> new UserNotificationData.SubwayArrInfoDto(
//                                                    a.subwayId(),
//                                                    a.updnLine(),
//                                                    a.statnNm(),
//                                                    a.barvlDt(),
//                                                    a.arvlMsg2(),
//                                                    a.arvlCd()
//                                            ))
//                                            .toList();
//                            return new UserNotificationData.SubwaySectionInfoDto(
//                                    section.subway().stationName(),
//                                    trains
//                            );
//                        }
//                        return null;
//                    })
//                    .filter(Objects::nonNull)
//                    .toList();
//
//            notifications.add(new UserNotificationData(
//                    token.getToken(),
//                    scheduleInfo.id(),
//                    scheduleInfo.name(),
//                    scheduleInfo.days(),
//                    sectionDtos
//            ));
//        }
//
//        if (notifications.isEmpty()) {
//            log.info("전송할 알림 없음");
//            return;
//        }
//
//        // FCM 메시지 전송
//        for (UserNotificationData notification : notifications) {
//            Message message = Message.builder()
//                    .setToken(notification.token())
//                    .putData("scheduleId", notification.scheduleId().toString())
//                    .putData("scheduleName", notification.scheduleName())
//                    .putData("days", notification.days().toString())
//                    .putData("sections", objectMapper.writeValueAsString(notification.sections()))
//                    .build();
//            log.info("{}", notification);
//            try {
//                String response = FirebaseMessaging.getInstance().send(message);
//                log.info("FCM 전송 성공: {}", response);
//            } catch (FirebaseMessagingException e) {
//                log.error("FCM 전송 실패: {}", e.getMessage());
//            }
//        }
//    }
//
//    public void sendTestNotification()
//            throws UnsupportedEncodingException, JsonProcessingException {
//        sendNotification();
//    }
//
//    @Transactional
//    public void deleteFCMToken(Long userId) {
//        Optional<FCMToken> fcmToken = fcmRepository.findByUserId(userId);
//        if (fcmToken.isPresent()) {
//            fcmRepository.delete(fcmToken.get());
//            return;
//        }
//        throw new FCMTokenException(ErrorCode.FCM_TOKEN_NOT_FOUND);
//    }
//}
