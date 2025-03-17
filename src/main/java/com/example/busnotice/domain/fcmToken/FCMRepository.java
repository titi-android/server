package com.example.busnotice.domain.fcmToken;

import com.example.busnotice.domain.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FCMRepository extends JpaRepository<FCMToken, Long> {

    Optional<FCMToken> findByUser(User user);

    Boolean existsByUser(User user);

    List<FCMToken> findAllByToken(String token);
}
