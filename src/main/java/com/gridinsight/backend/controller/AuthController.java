package com.gridinsight.backend.controller;

import com.gridinsight.backend.dto.RegisterRequest;
import com.gridinsight.backend.service.AuthService;
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

//    public AuthController(AuthService authService) {
//        this.authService = authService;
//    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<com.gridinsight.backend.dto.LoginResponse> login(
            @Valid @RequestBody com.gridinsight.backend.dto.LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<com.gridinsight.backend.dto.RefreshResponse> refresh(
            @Valid @RequestBody com.gridinsight.backend.dto.RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req));
    }


    @PostMapping("/password/forgot")
    public ResponseEntity<com.gridinsight.backend.dto.PasswordResetResponse> forgotPassword(
            @Valid @RequestBody com.gridinsight.backend.dto.PasswordResetRequest req,
            jakarta.servlet.http.HttpServletRequest request) {
        return ResponseEntity.ok(authService.requestPasswordReset(req, request.getRemoteAddr()));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody com.gridinsight.backend.dto.PasswordResetConfirmRequest req,
            jakarta.servlet.http.HttpServletRequest request) {
        authService.resetPassword(req, request.getRemoteAddr());
        return ResponseEntity.ok().build();
    }
}