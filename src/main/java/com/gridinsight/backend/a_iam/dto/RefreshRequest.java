package com.gridinsight.backend.a_iam.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequest {
    @NotBlank(message = "refreshToken is required")
    private String refreshToken;
}

