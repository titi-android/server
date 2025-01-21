package com.example.busnotice.domain.fcmToken.req;

import com.example.busnotice.domain.fcmToken.FCMToken;
import com.example.busnotice.domain.user.User;

public record CreateFCMTokenRequest(
    String token
) {

    private FCMToken toEntity(User user) {
        return new FCMToken(user, this.token);
    }
}
