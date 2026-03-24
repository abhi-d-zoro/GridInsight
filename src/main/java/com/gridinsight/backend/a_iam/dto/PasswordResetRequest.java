package com.gridinsight.backend.a_iam.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequest {
    @NotBlank(message = "email or phone is required")
    private String identifier;
}

