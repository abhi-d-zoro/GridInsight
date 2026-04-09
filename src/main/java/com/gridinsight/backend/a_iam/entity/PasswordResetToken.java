package com.gridinsight.backend.a_iam.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "password_reset_tokens",
        indexes = {
                @Index(name = "idx_password_reset_user", columnList = "user_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --------------------------------------------------
    // User this OTP belongs to
    // --------------------------------------------------
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // --------------------------------------------------
    // Hashed OTP (never store plain OTP)
    // SHA-256 → 64 chars
    // --------------------------------------------------
    @Column(name = "otp_hash", nullable = false, length = 64)
    private String otpHash;

    // --------------------------------------------------
    // OTP expiry
    // --------------------------------------------------
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    // --------------------------------------------------
    // Number of verification attempts
    // --------------------------------------------------
    @Column(name = "attempts", nullable = false)
    private int attempts;

    // --------------------------------------------------
    // When OTP was successfully used
    // --------------------------------------------------
    @Column(name = "used_at")
    private Instant usedAt;

    // --------------------------------------------------
    // Creation timestamp
    // --------------------------------------------------
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // --------------------------------------------------
    // Helper checks (optional but useful)
    // --------------------------------------------------
    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public boolean isUsed() {
        return usedAt != null;
    }
}
