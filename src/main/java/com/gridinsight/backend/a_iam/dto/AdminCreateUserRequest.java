 package com.gridinsight.backend.a_iam.dto;

 import jakarta.validation.constraints.Email;
 import jakarta.validation.constraints.NotBlank;

public record AdminCreateUserRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        String phone,
        @NotBlank String role,          // e.g., "PLANNER", "ADMIN", "GRID_ANALYST"
        @NotBlank String tempPassword   // or let backend generate one if omitted
) {}