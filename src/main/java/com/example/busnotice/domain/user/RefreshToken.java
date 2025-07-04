package com.example.busnotice.domain.user;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    public RefreshToken(User user, String refreshToken) {
        this.user = user;
        this.token = refreshToken;
    }

    public RefreshToken() {

    }

    public void update(String newToken) {
        this.token = newToken;
    }
}
