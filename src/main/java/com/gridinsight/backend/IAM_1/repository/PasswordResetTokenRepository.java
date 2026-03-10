package com.gridinsight.backend.IAM_1.repository;

import com.gridinsight.backend.IAM_1.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update PasswordResetToken t set t.usedAt = :usedAt where t.user.id = :userId and t.usedAt is null and t.expiresAt > :now")
    int invalidateActiveTokens(@Param("userId") Long userId,
                               @Param("now") Instant now,
                               @Param("usedAt") Instant usedAt);
}

