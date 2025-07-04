package com.example.busnotice.domain.fcmToken;

import com.example.busnotice.domain.user.User;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class FCMToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(unique = true, name = "user_id")
    private User user;

    @Column
    private String token;

    public FCMToken() {

    }

    public void update(String token) {
        this.token = token;
    }

    public FCMToken(User user, String token) {
        this.user = user;
        this.token = token;
    }
}
