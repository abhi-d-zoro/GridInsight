package com.gridinsight.backend.IAM_1.controller;

import com.gridinsight.backend.IAM_1.dto.LoginRequest;
import com.gridinsight.backend.IAM_1.dto.LoginResponse;
import com.gridinsight.backend.IAM_1.dto.RefreshRequest;
import com.gridinsight.backend.IAM_1.dto.RefreshResponse;
import com.gridinsight.backend.IAM_1.dto.PasswordResetRequest;
import com.gridinsight.backend.IAM_1.dto.PasswordResetConfirmRequest;
import com.gridinsight.backend.IAM_1.dto.PasswordResetResponse;
import com.gridinsight.backend.IAM_1.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // PUBLIC: Login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    // PUBLIC: Refresh access token
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req));
    }

    // PUBLIC: Request password reset (generates a reset token)
    @PostMapping("/password/otp")
    public ResponseEntity<PasswordResetResponse> forgotPassword(
            @Valid @RequestBody PasswordResetRequest req,
            HttpServletRequest request) {
        return ResponseEntity.ok(authService.requestPasswordReset(req, request.getRemoteAddr()));
    }

    // PUBLIC: Confirm password reset using the reset token
    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody PasswordResetConfirmRequest req,
            HttpServletRequest request) {
        authService.resetPassword(req, request.getRemoteAddr());
        return ResponseEntity.ok().build();
    }
}
