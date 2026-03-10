package com.gridinsight.backend.IAM_1.service;

import com.gridinsight.backend.IAM_1.dto.*;
import com.gridinsight.backend.IAM_1.entity.*;
import com.gridinsight.backend.IAM_1.exception.AccountLockedException;
import com.gridinsight.backend.IAM_1.exception.UnauthorizedException;
import com.gridinsight.backend.IAM_1.repository.*;
import com.gridinsight.backend.IAM_1.security.JwtService;
import com.gridinsight.backend.IAM_1.security.TokenHasher;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_MINUTES = 15;
    private static final long ACCESS_TOKEN_TTL_SECONDS = 30 * 60;
    private static final long REFRESH_TOKEN_TTL_SECONDS = 7 * 24 * 60 * 60;
    private static final long PASSWORD_RESET_TTL_SECONDS = 10 * 60;
    private static final String PASSWORD_POLICY_MESSAGE = "Password must be at least 8 chars and include upper, lower, number, and special character";
    private static final java.util.regex.Pattern PASSWORD_POLICY =
            java.util.regex.Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$");

    @Transactional
    public void register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("Email already in use");
        }

        validatePasswordPolicy(req.getPassword());


        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .passwordHash(encoder.encode(req.getPassword()))
                .status(UserStatus.ACTIVE)
                .failedAttempts(0)
                .lockUntil(null)
                .build();

        userRepo.save(user);
    }


    @Transactional(dontRollbackOn = {
            UnauthorizedException.class,
            AccountLockedException.class
    })
    public LoginResponse login(LoginRequest req) {

        String identifier = req.getIdentifier();
        User user = userRepo.findByEmailOrPhone(identifier, identifier)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

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

        String accessToken = jwtService.generateAccessToken(user.getId(), ACCESS_TOKEN_TTL_SECONDS);
        String refreshToken = createRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken, "Bearer", ACCESS_TOKEN_TTL_SECONDS);
    }

    @Transactional
    public RefreshResponse refresh(RefreshRequest req) {
        String tokenHash = TokenHasher.sha256(req.getRefreshToken());
        RefreshToken existing = refreshTokenRepo.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (existing.getRevokedAt() != null || existing.getExpiresAt().isBefore(java.time.Instant.now())) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String newRefresh = createRefreshToken(existing.getUser());
        existing.setRevokedAt(java.time.Instant.now());
        existing.setReplacedByTokenHash(TokenHasher.sha256(newRefresh));
        refreshTokenRepo.save(existing);

        String accessToken = jwtService.generateAccessToken(existing.getUser().getId(), ACCESS_TOKEN_TTL_SECONDS);
        return new RefreshResponse(accessToken, newRefresh, "Bearer", ACCESS_TOKEN_TTL_SECONDS);
    }

    @Transactional
    public PasswordResetResponse requestPasswordReset(
            PasswordResetRequest req, String ipAddress) {
        String identifier = req.getIdentifier();
        java.util.Optional<User> userOpt = userRepo.findByEmailOrPhone(identifier, identifier);

        if (userOpt.isEmpty()) {
            auditPasswordReset(null, "PASSWORD_RESET_REQUEST", identifier, ipAddress, false, "User not found");
            return new PasswordResetResponse(
                    "If the account exists, a reset token has been generated.", null);
        }

        User user = userOpt.get();
        java.time.Instant now = java.time.Instant.now();
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
                "If the account exists, a reset token has been generated.", rawToken);
    }

    @Transactional
    public void resetPassword(PasswordResetConfirmRequest req, String ipAddress) {
        String tokenHash = TokenHasher.sha256(req.getToken());
        PasswordResetToken token = passwordResetTokenRepo.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        java.time.Instant now = java.time.Instant.now();
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(now)) {
            auditPasswordReset(token.getUser(), "PASSWORD_RESET_FAILURE", null, ipAddress, false, "Token expired or used");
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        validatePasswordPolicy(req.getNewPassword());

        User user = token.getUser();
        user.setPasswordHash(encoder.encode(req.getNewPassword()));
        userRepo.save(user);

        token.setUsedAt(now);
        passwordResetTokenRepo.save(token);

        auditPasswordReset(user, "PASSWORD_RESET_SUCCESS", null, ipAddress, true, null);
    }

    private boolean isLocked(User user) {
        return user.getLockUntil() != null && user.getLockUntil().isAfter(java.time.Instant.now());
    }

    private void registerFailedAttempt(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockUntil(java.time.Instant.now().plusSeconds(LOCK_MINUTES * 60));
        }
        userRepo.save(user);
    }

    private void resetFailedAttempts(User user) {
        user.setFailedAttempts(0);
        user.setLockUntil(null);
        userRepo.save(user);
    }

    private String createRefreshToken(User user) {
        String raw = java.util.UUID.randomUUID().toString() + java.util.UUID.randomUUID();
        String hash = TokenHasher.sha256(raw);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(java.time.Instant.now().plusSeconds(REFRESH_TOKEN_TTL_SECONDS))
                .createdAt(java.time.Instant.now())
                .build();
        refreshTokenRepo.save(token);

        return raw;
    }

    private void auditLogin(User user, String identifier, boolean success, String reason) {
        LoginAudit audit = LoginAudit.builder()
                .user(user)
                .identifier(identifier)
                .success(success)
                .reason(reason)
                .createdAt(java.time.Instant.now())
                .build();
        loginAuditRepo.save(audit);
    }

    private String generateResetToken() {
        return java.util.UUID.randomUUID().toString() + java.util.UUID.randomUUID();
    }

    private void validatePasswordPolicy(String password) {
        if (!PASSWORD_POLICY.matcher(password).matches()) {
            throw new IllegalArgumentException(PASSWORD_POLICY_MESSAGE);
        }
    }

    private void auditPasswordReset(User user, String action, String identifier, String ipAddress, boolean success, String reason) {
        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        if (identifier != null) {
            metadata.put("identifier", identifier);
        }
        metadata.put("success", success);
        if (reason != null) {
            metadata.put("reason", reason);
        }

        String metadataJson;
        try {
            metadataJson = objectMapper.writeValueAsString(metadata);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            metadataJson = "{}";
        }

        String correlationId = java.util.UUID.randomUUID().toString();

        AuditLog audit = AuditLog.builder()
                .actorUserId(user != null ? user.getId() : null)
                .targetUserId(user != null ? user.getId() : null)
                .action(action)
                .resource("auth/password-reset")
                .timestamp(java.time.Instant.now())
                .metadata(metadataJson)
                .correlationId(correlationId)
                .ipAddress(ipAddress)
                .build();
        auditLogRepo.save(audit);
    }
}
