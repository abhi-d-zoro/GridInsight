package com.gridinsight.backend.IAM_1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResetResponse {
    private String message;
    private String resetToken;
}

