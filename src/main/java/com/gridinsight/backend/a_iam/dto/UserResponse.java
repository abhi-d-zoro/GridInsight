package com.gridinsight.backend.a_iam.dto;

import com.gridinsight.backend.a_iam.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private UserStatus status;

    /**
     * ✅ SINGLE ROLE
     * Example: "ADMIN", "ESG", "PLANNER"
     */
    private String role;

    private Instant createdAt;
    private Instant updatedAt;
}
