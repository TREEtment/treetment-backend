package com.treetment.backend.auth.repository;

import com.treetment.backend.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenAndIsUsedFalseAndExpiresAtAfter(String token, LocalDateTime now);
    void deleteByEmail(String email);
}
