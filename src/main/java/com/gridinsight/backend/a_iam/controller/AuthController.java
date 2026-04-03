package com.gridinsight.backend.a_iam.controller;

import com.gridinsight.backend.a_iam.dto.*;
import com.gridinsight.backend.a_iam.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
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
