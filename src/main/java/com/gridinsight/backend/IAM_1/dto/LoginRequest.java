package com.gridinsight.backend.IAM_1.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "email or phone is required")
    private String identifier;

    @NotBlank(message = "password is required")
    private String password;
}
