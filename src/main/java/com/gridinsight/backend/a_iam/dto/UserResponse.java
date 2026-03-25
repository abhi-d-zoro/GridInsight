package com.gridinsight.backend.a_iam.dto;

import com.gridinsight.backend.a_iam.entity.UserStatus;

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
