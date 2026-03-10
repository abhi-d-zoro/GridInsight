package com.gridinsight.backend.IAM_1.dto;

import com.gridinsight.backend.IAM_1.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private UserStatus status;
    private Set<String> roles;
    private Instant createdAt;
    private Instant updatedAt;
}

