package com.gridinsight.backend.a_iam.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "name is required")
    private String name;

    @Email @NotBlank(message = "email is required")
    private String email;

    private String phone;

    @NotBlank @Size(min = 8, message = "password must be at least 8 chars")
    private String password;


}