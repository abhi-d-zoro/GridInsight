package com.gridinsight.backend.a_iam.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Used when user clicks "Forgot Password"
 * Sends OTP to the given email.
 */
@Data
public class PasswordOtpRequest {

    @NotBlank(message = "email is required")
    @Email(message = "invalid email format")
    private String email;
}