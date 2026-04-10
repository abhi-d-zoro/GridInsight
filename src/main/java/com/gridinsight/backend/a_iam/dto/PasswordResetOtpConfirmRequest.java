package com.gridinsight.backend.a_iam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Used to verify OTP and reset password.
 */
@Data
public class PasswordResetOtpConfirmRequest {

    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "OTP is required")
    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "OTP must be a 6-digit number"
    )
    private String otp;

    @NotBlank(message = "new password is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    private String newPassword;
}
