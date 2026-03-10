package com.gridinsight.backend.IAM_1.controller;

import com.gridinsight.backend.IAM_1.dto.*;
import com.gridinsight.backend.IAM_1.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;



    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(
            @Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req));
    }


    @PostMapping("/password/forgot")
    public ResponseEntity<PasswordResetResponse> forgotPassword(
            @Valid @RequestBody PasswordResetRequest req,
            jakarta.servlet.http.HttpServletRequest request) {
        return ResponseEntity.ok(authService.requestPasswordReset(req, request.getRemoteAddr()));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody PasswordResetConfirmRequest req,
            jakarta.servlet.http.HttpServletRequest request) {
        authService.resetPassword(req, request.getRemoteAddr());
        return ResponseEntity.ok().build();
    }
}