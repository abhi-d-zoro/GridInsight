package com.gridinsight.backend.a_iam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResetResponse {
    private String message;
    private String resetToken;
}

