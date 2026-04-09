package com.gridinsight.backend.a_iam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Generic public response message
 * (used for OTP requests & password reset success)
 */
@Data
@AllArgsConstructor
public class MessageResponse {
    private String message;
}
