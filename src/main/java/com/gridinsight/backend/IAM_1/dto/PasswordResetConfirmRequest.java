package com.gridinsight.backend.IAM_1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetConfirmRequest {
    @NotBlank(message = "reset token is required")
    private String token;

    @NotBlank(message = "new password is required")
    @Size(min = 8, message = "password must be at least 8 chars")
    private String newPassword;
}

