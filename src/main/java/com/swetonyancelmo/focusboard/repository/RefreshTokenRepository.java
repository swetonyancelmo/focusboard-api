package com.swetonyancelmo.focusboard.repository;

import com.swetonyancelmo.focusboard.model.RefreshToken;
import com.swetonyancelmo.focusboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
