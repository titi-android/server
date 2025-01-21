package com.example.busnotice.domain.fcmToken;

import com.example.busnotice.domain.fcmToken.req.CreateFCMTokenRequest;
import com.example.busnotice.domain.user.User;
import com.example.busnotice.domain.user.UserRepository;
import com.example.busnotice.global.code.StatusCode;
import com.example.busnotice.global.exception.UserException;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {


    private final FCMRepository fcmRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createFCMToken(Long userId, CreateFCMTokenRequest createFCMTokenRequest) {
        User user = userRepository.findById(userId).orElseThrow(
            () -> new UserException(StatusCode.NOT_FOUND, "해당 ID 의 유저가 존재하지 않습니다.")
        );

        Optional<FCMToken> optionalFCMToken = fcmRepository.findByUser(user);
        // 기존 토큰 존재하는 경우
        if (optionalFCMToken.isPresent()) {
            FCMToken fcmToken = optionalFCMToken.get();
            fcmToken.update(createFCMTokenRequest.token());
            return;
        }
        // 그렇지 않은 경우
        fcmRepository.save(new FCMToken(user, createFCMTokenRequest.token()));
    }

    public void sendNotification(String token) {

        Map<String, String> data = new HashMap<>();
        data.put("test title", String.valueOf("test content")); // 이상 상황(Info) ID

        // FCM에 보낼 메시지 빌드
        MulticastMessage fcmMessage = MulticastMessage.builder()
            .addToken(token)
            .putAllData(data) // 추가 데이터
            .build();

        log.info("fcmMessage: {}", fcmMessage);
        try {
            // 메시지 전송
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(fcmMessage);
            List<String> successfulIds = new ArrayList<>();
            List<String> failedIds = new ArrayList<>();

            for (SendResponse sendResponse : response.getResponses()) {
                if (sendResponse.isSuccessful()) {
                    successfulIds.add(sendResponse.getMessageId());
                } else {
                    failedIds.add(sendResponse.getException().getMessage());
                }
            }

            System.out.println(
                "Successfully sent messages with IDs: " + String.join(", ", successfulIds));
            if (!failedIds.isEmpty()) {
                System.err.println("Failed to send messages: " + String.join(", ", failedIds));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
