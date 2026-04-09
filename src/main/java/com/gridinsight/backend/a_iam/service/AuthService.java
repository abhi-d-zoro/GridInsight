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

import java.security.SecureRandom;
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

    // --------------------------------------------------
    // Security policies
    // --------------------------------------------------
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_MINUTES = 15;

    private static final long ACCESS_TOKEN_TTL_SECONDS = 30 * 60;
    private static final long REFRESH_TOKEN_TTL_SECONDS = 7 * 24 * 60 * 60;
    private static final long REFRESH_IDLE_TIMEOUT_MINUTES = 30;

    // OTP policy
    private static final int OTP_LENGTH = 6;
    private static final int OTP_MAX_ATTEMPTS = 5;
    private static final long OTP_TTL_SECONDS = 10 * 60;

    private static final String PASSWORD_POLICY_MESSAGE =
            "Password must be at least 8 chars and include upper, lower, number, and special character";

    private static final java.util.regex.Pattern PASSWORD_POLICY =
            java.util.regex.Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$");

    // ==================================================
    // LOGIN
    // ==================================================
    @Transactional(dontRollbackOn = { UnauthorizedException.class, AccountLockedException.class })
    public LoginResponse login(LoginRequest req) {

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

        String roleName = user.getRole().getName();
        Set<String> roles = Set.of(roleName);

        String accessToken =
                jwtService.generateAccessToken(user.getId(), roles, ACCESS_TOKEN_TTL_SECONDS);

        String refreshToken = createRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken, "Bearer", ACCESS_TOKEN_TTL_SECONDS);
    }

    // ==================================================
    // REFRESH TOKEN
    // ==================================================
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

    // ==================================================
    // OTP PASSWORD RESET (REAL-WORLD FLOW)
    // ==================================================

    // STEP 1: Generate & send OTP
    @Transactional
    public void sendPasswordResetOtp(String email, String ipAddress) {

        var userOpt = userRepo.findByEmail(email);

        // Always return generic success response (anti-user-enumeration)
        if (userOpt.isEmpty()) {
            auditPasswordReset(null, "PASSWORD_OTP_REQUEST", email, ipAddress, false, "User not found");
            return;
        }

        User user = userOpt.get();
        Instant now = Instant.now();

        passwordResetTokenRepo.invalidateAllOtps(user.getId(), now);

        String otp = generateOtp();
        String otpHash = TokenHasher.sha256(otp);

        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .otpHash(otpHash)
                .expiresAt(now.plusSeconds(OTP_TTL_SECONDS))
                .attempts(0)
                .createdAt(now)
                .build();

        passwordResetTokenRepo.save(token);

        /*
         * TODO: Send OTP via email service
         * emailService.sendOtp(user.getEmail(), otp);
         */
        System.out.println("PASSWORD RESET OTP (DEV ONLY): " + otp);

        auditPasswordReset(user, "PASSWORD_OTP_SENT", email, ipAddress, true, null);
    }

    // STEP 2: Verify OTP & reset password
    @Transactional
    public void verifyOtpAndResetPassword(
            PasswordResetOtpConfirmRequest req,
            String ipAddress) {

        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP or expired"));

        PasswordResetToken token = passwordResetTokenRepo
                .findActiveOtpByUser(user.getId(), Instant.now())
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP or expired"));

        if (token.getAttempts() >= OTP_MAX_ATTEMPTS) {
            auditPasswordReset(user,
                    "PASSWORD_OTP_LOCKED", req.getEmail(), ipAddress,
                    false, "Max attempts exceeded");
            throw new IllegalArgumentException("OTP attempts exceeded");
        }

        String otpHash = TokenHasher.sha256(req.getOtp());

        if (!otpHash.equals(token.getOtpHash())) {
            token.setAttempts(token.getAttempts() + 1);
            passwordResetTokenRepo.save(token);

            auditPasswordReset(user,
                    "PASSWORD_OTP_FAILURE", req.getEmail(), ipAddress,
                    false, "Invalid OTP");
            throw new IllegalArgumentException("Invalid OTP");
        }

        validatePasswordPolicy(req.getNewPassword());

        user.setPasswordHash(encoder.encode(req.getNewPassword()));
        userRepo.save(user);

        token.setUsedAt(Instant.now());
        passwordResetTokenRepo.save(token);

        auditPasswordReset(user,
                "PASSWORD_RESET_SUCCESS", req.getEmail(), ipAddress,
                true, null);
    }

    // ==================================================
    // HELPERS
    // ==================================================
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

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = random.nextInt((int) Math.pow(10, OTP_LENGTH));
        return String.format("%06d", otp);
    }

    private void validatePasswordPolicy(String password) {
        if (!PASSWORD_POLICY.matcher(password).matches()) {
            throw new IllegalArgumentException(PASSWORD_POLICY_MESSAGE);
        }
    }

    // ==================================================
    // AUDIT HELPERS
    // ==================================================
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
