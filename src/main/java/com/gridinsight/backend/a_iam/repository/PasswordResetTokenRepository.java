package com.gridinsight.backend.a_iam.repository;

import com.gridinsight.backend.a_iam.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    // --------------------------------------------------
    // Fetch the latest ACTIVE OTP for a user
    // - not used
    // - not expired
    // --------------------------------------------------
    @Query("""
        select t from PasswordResetToken t
        where t.user.id = :userId
          and t.usedAt is null
          and t.expiresAt > :now
        order by t.createdAt desc
        """)
    Optional<PasswordResetToken> findActiveOtpByUser(
            @Param("userId") Long userId,
            @Param("now") Instant now
    );

    // --------------------------------------------------
    // Invalidate ALL existing OTPs for a user
    // Used when generating a new OTP
    // --------------------------------------------------
    @Modifying
    @Query("""
        update PasswordResetToken t
           set t.usedAt = :now
         where t.user.id = :userId
           and t.usedAt is null
        """)
    int invalidateAllOtps(
            @Param("userId") Long userId,
            @Param("now") Instant now
    );
}