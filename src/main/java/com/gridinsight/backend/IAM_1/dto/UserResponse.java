package com.gridinsight.backend.IAM_1.dto;

import com.gridinsight.backend.IAM_1.entity.UserStatus;
import java.time.Instant;
import java.util.Set;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        UserStatus status,
        Set<String> roles,
        Instant createdAt,
        Instant updatedAt
) {}
