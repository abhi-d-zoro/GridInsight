package com.gridinsight.backend.a_iam.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private String name;

    @Email(message = "Email must be valid")
    private String email;

    @Size(min = 10, max = 15, message = "Phone must be between 10 and 15 characters")
    private String phone;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /**
     * ✅ SINGLE ROLE per user
     * Example: "ADMIN", "ESG", "PLANNER"
     */
    private String role;

    private String status;
}
