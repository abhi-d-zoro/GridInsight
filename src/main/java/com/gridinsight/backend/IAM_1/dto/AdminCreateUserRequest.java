 package com.gridinsight.backend.IAM_1.dto;

import jakarta.validation.constraints.*;

public record AdminCreateUserRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        String phone,
        @NotBlank String role,          // e.g., "PLANNER", "ADMIN", "GRID_ANALYST"
        @NotBlank String tempPassword   // or let backend generate one if omitted
) {}