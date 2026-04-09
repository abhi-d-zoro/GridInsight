package com.gridinsight.backend.a_iam.controller;

import com.gridinsight.backend.a_iam.dto.*;
import com.gridinsight.backend.a_iam.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // --------------------------------------------------
    // PUBLIC: Login
    // --------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    // --------------------------------------------------
    // PUBLIC: Refresh access token
    // --------------------------------------------------
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(
            @Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req));
    }

    // --------------------------------------------------
    // PUBLIC: Forgot password → Send OTP to email
    // --------------------------------------------------
    @PostMapping("/password/otp")
    public ResponseEntity<MessageResponse> requestPasswordOtp(
            @Valid @RequestBody PasswordOtpRequest req,
            HttpServletRequest httpRequest) {

        authService.sendPasswordResetOtp(
                req.getEmail(),
                httpRequest.getRemoteAddr()
        );

        // ✅ Always return generic message
        return ResponseEntity.ok(
                new MessageResponse(
                        "If the account exists, an OTP has been sent to your email."
                )
        );
    }

    // --------------------------------------------------
    // PUBLIC: Verify OTP + Reset password
    // --------------------------------------------------
    @PostMapping("/password/reset")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody PasswordResetOtpConfirmRequest req,
            HttpServletRequest httpRequest) {

        authService.verifyOtpAndResetPassword(
                req,
                httpRequest.getRemoteAddr()
        );

        return ResponseEntity.ok(
                new MessageResponse("Password reset successful.")
        );
    }
}
