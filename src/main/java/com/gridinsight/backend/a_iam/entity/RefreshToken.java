package com.gridinsight.backend.a_iam.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // explicit FK column name
    private User user;

    /**
     * SHA-256 hex (64 chars) of the raw refresh token string.
     * Unique to prevent duplicates.
     */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_token_hash", length = 64)
    private String replacedByTokenHash;

    /**
     * NEW: used for server-side idle timeout checks.
     * Set to 'now' at creation; evaluated at /auth/refresh.
     */
    @Column(name = "last_used_at")
    private Instant lastUsedAt;
}