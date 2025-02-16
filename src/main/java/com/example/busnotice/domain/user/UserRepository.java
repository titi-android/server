package com.example.busnotice.domain.user;

import java.util.Optional;
import javax.swing.text.html.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByName(String name);

    Optional<User> findByName(String name);

    @Query("SELECT u FROM User u WHERE u.name = :name AND u.id <> :userId")
    Optional<User> findByNameWithoutMe(@Param("userId") Long userId, @Param("name") String name);
}
