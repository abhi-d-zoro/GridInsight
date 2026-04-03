package com.gridinsight.backend.a_iam.service;

import com.gridinsight.backend.a_iam.dto.*;
import com.gridinsight.backend.a_iam.entity.*;
import com.gridinsight.backend.a_iam.exception.AccountLockedException;
import com.gridinsight.backend.a_iam.exception.UnauthorizedException;
import com.gridinsight.backend.a_iam.repository.LoginAuditRepository;
import com.gridinsight.backend.a_iam.repository.PasswordResetTokenRepository;
import com.gridinsight.backend.a_iam.repository.RefreshTokenRepository;
import com.gridinsight.backend.a_iam.repository.UserRepository;
import com.gridinsight.backend.a_iam.security.JwtService;
import com.gridinsight.backend.z_common.audit.AuditLog;
import com.gridinsight.backend.z_common.audit.AuditLogRepository;
import com.gridinsight.backend.z_common.util.TokenHasher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepo;
    private final LoginAuditRepository loginAuditRepo;
    private final PasswordResetTokenRepository passwordResetTokenRepo;
    private final AuditLogRepository auditLogRepo;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    // ==== Security policies ====
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_MINUTES = 15;

    private static final long ACCESS_TOKEN_TTL_SECONDS = 30 * 60;
    private static final long REFRESH_TOKEN_TTL_SECONDS = 7 * 24 * 60 * 60;
    private static final long REFRESH_IDLE_TIMEOUT_MINUTES = 30;
    private static final long PASSWORD_RESET_TTL_SECONDS = 10 * 60;

    private static final String PASSWORD_POLICY_MESSAGE =
            "Password must be at least 8 chars and include upper, lower, number, and special character";

    private static final java.util.regex.Pattern PASSWORD_POLICY =
            java.util.regex.Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$");

    // --------------------------------------------------
    // LOGIN
    // --------------------------------------------------
    @Transactional(dontRollbackOn = { UnauthorizedException.class, AccountLockedException.class })
    public LoginResponse login(LoginRequest req) {

        System.out.println(
                encoder.matches(
                        "Admin@12345",
                        "$2a$10$FoUKiMRgiK7i79NWR2wWzeRJqMZNLTWrS.rstuh8LP6FQiDolFamC"
                )
        );


        String identifier = req.getEmail();
        User user = userRepo.findByEmailOrPhone(identifier, identifier)
                .orElseThrow(() -> {
                    auditLogin(null, identifier, false, "Invalid credentials");
                    return new UnauthorizedException("Invalid credentials");
                });

        if (isLocked(user)) {
            auditLogin(user, identifier, false, "Account locked");
            throw new AccountLockedException("Account locked. Try again later.");
        }

        if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
            registerFailedAttempt(user);
            auditLogin(user, identifier, false, "Invalid credentials");
            throw new UnauthorizedException("Invalid credentials");
        }

        resetFailedAttempts(user);
        auditLogin(user, identifier, true, null);

        // ✅ SINGLE ROLE → JWT wants SET
        String roleName = user.getRole().getName();
        Set<String> roles = Set.of(roleName);

        String accessToken =
                jwtService.generateAccessToken(user.getId(), roles, ACCESS_TOKEN_TTL_SECONDS);

        String refreshToken = createRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken, "Bearer", ACCESS_TOKEN_TTL_SECONDS);
    }

    // --------------------------------------------------
    // REFRESH TOKEN
    // --------------------------------------------------
    @Transactional
    public RefreshResponse refresh(RefreshRequest req) {

        String tokenHash = TokenHasher.sha256(req.getRefreshToken());
        RefreshToken existing = refreshTokenRepo.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        Instant now = Instant.now();

        if (existing.getRevokedAt() != null || existing.getExpiresAt().isBefore(now)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        if (existing.getLastUsedAt() == null ||
                Duration.between(existing.getLastUsedAt(), now)
                        .toMinutes() > REFRESH_IDLE_TIMEOUT_MINUTES) {

            existing.setRevokedAt(now);
            refreshTokenRepo.save(existing);
            throw new UnauthorizedException("Session idle timeout. Please login again.");
        }

        String newRefresh = createRefreshToken(existing.getUser());
        existing.setRevokedAt(now);
        existing.setReplacedByTokenHash(TokenHasher.sha256(newRefresh));
        refreshTokenRepo.save(existing);

        String roleName = existing.getUser().getRole().getName();
        Set<String> roles = Set.of(roleName);

        String accessToken =
                jwtService.generateAccessToken(existing.getUser().getId(), roles, ACCESS_TOKEN_TTL_SECONDS);

        return new RefreshResponse(accessToken, newRefresh, "Bearer", ACCESS_TOKEN_TTL_SECONDS);
    }

    // --------------------------------------------------
    // PASSWORD RESET
    // --------------------------------------------------
    @Transactional
    public PasswordResetResponse requestPasswordReset(PasswordResetRequest req, String ipAddress) {

        String identifier = req.getIdentifier();
        var userOpt = userRepo.findByEmailOrPhone(identifier, identifier);

        if (userOpt.isEmpty()) {
            auditPasswordReset(null, "PASSWORD_RESET_REQUEST",
                    identifier, ipAddress, false, "User not found");
            return new PasswordResetResponse(
                    "If the account exists, a reset token has been generated.", null
            );
        }

        User user = userOpt.get();
        Instant now = Instant.now();

        passwordResetTokenRepo.invalidateActiveTokens(user.getId(), now, now);

        String rawToken = generateResetToken();
        String tokenHash = TokenHasher.sha256(rawToken);

        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(now.plusSeconds(PASSWORD_RESET_TTL_SECONDS))
                .createdAt(now)
                .build();

        passwordResetTokenRepo.save(token);

        auditPasswordReset(user, "PASSWORD_RESET_REQUEST", identifier, ipAddress, true, null);
        return new PasswordResetResponse(
                "If the account exists, a reset token has been generated.", rawToken
        );
    }

    @Transactional
    public void resetPassword(PasswordResetConfirmRequest req, String ipAddress) {

        String tokenHash = TokenHasher.sha256(req.getToken());
        PasswordResetToken token = passwordResetTokenRepo.findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid or expired reset token")
                );

        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            auditPasswordReset(token.getUser(),
                    "PASSWORD_RESET_FAILURE", null, ipAddress, false,
                    "Token expired or used");
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        validatePasswordPolicy(req.getNewPassword());

        User user = token.getUser();
        user.setPasswordHash(encoder.encode(req.getNewPassword()));
        userRepo.save(user);

        token.setUsedAt(Instant.now());
        passwordResetTokenRepo.save(token);

        auditPasswordReset(user,
                "PASSWORD_RESET_SUCCESS", null, ipAddress, true, null);
    }

    // --------------------------------------------------
    // HELPERS
    // --------------------------------------------------
    private boolean isLocked(User user) {
        return user.getLockUntil() != null
                && user.getLockUntil().isAfter(Instant.now());
    }

    private void registerFailedAttempt(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockUntil(
                    Instant.now().plusSeconds(LOCK_MINUTES * 60)
            );
        }
        userRepo.save(user);
    }

    private void resetFailedAttempts(User user) {
        user.setFailedAttempts(0);
        user.setLockUntil(null);
        userRepo.save(user);
    }

    private String createRefreshToken(User user) {
        String raw = java.util.UUID.randomUUID() + "" + java.util.UUID.randomUUID();
        String hash = TokenHasher.sha256(raw);
        Instant now = Instant.now();

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(now.plusSeconds(REFRESH_TOKEN_TTL_SECONDS))
                .createdAt(now)
                .lastUsedAt(now)
                .build();

        refreshTokenRepo.save(token);
        return raw;
    }

    private String generateResetToken() {
        return java.util.UUID.randomUUID() + "" + java.util.UUID.randomUUID();
    }

    private void validatePasswordPolicy(String password) {
        if (!PASSWORD_POLICY.matcher(password).matches()) {
            throw new IllegalArgumentException(PASSWORD_POLICY_MESSAGE);
        }
    }

    private void auditLogin(User user, String identifier, boolean success, String reason) {

        LoginAudit audit = LoginAudit.builder()
                .user(user)
                .identifier(identifier)
                .success(success)
                .reason(reason)
                .createdAt(Instant.now())
                .build();
        loginAuditRepo.save(audit);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("identifier", identifier);
        metadata.put("success", success);
        if (reason != null) metadata.put("reason", reason);

        try {
            AuditLog al = AuditLog.builder()
                    .actorUserId(user != null ? user.getId() : null)
                    .targetUserId(user != null ? user.getId() : null)
                    .action(success ? "LOGIN_SUCCESS" : "LOGIN_FAILURE")
                    .resource("auth/login")
                    .timestamp(Instant.now())
                    .metadata(objectMapper.writeValueAsString(metadata))
                    .correlationId(java.util.UUID.randomUUID().toString())
                    .ipAddress(null)
                    .build();

            auditLogRepo.save(al);

        } catch (Exception ignored) {}
    }

    private void auditPasswordReset(
            User user, String action, String identifier,
            String ipAddress, boolean success, String reason) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("success", success);
        if (identifier != null) metadata.put("identifier", identifier);
        if (reason != null) metadata.put("reason", reason);

        try {
            AuditLog audit = AuditLog.builder()
                    .actorUserId(user != null ? user.getId() : null)
                    .targetUserId(user != null ? user.getId() : null)
                    .action(action)
                    .resource("auth/password-reset")
                    .timestamp(Instant.now())
                    .metadata(objectMapper.writeValueAsString(metadata))
                    .correlationId(java.util.UUID.randomUUID().toString())
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepo.save(audit);

        } catch (Exception ignored) {}
    }
}