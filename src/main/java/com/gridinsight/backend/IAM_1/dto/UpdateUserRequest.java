package com.gridinsight.backend.IAM_1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRequest {
    private String name;

    @Email(message = "Email must be valid")
    private String email;

    @Size(min = 10, max = 15, message = "Phone must be between 10 and 15 characters")
    private String phone;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private Set<String> roles;

    private String status;
}

