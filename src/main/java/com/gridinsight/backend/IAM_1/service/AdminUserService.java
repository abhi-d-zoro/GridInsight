 package com.gridinsight.backend.IAM_1.service;

import com.gridinsight.backend.IAM_1.dto.AdminCreateUserRequest;
import com.gridinsight.backend.IAM_1.dto.UserResponse;
import com.gridinsight.backend.IAM_1.entity.Role;
import com.gridinsight.backend.IAM_1.entity.User;
import com.gridinsight.backend.IAM_1.entity.UserStatus;
import com.gridinsight.backend.IAM_1.repository.RoleRepository;
import com.gridinsight.backend.IAM_1.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    @Transactional
    public UserResponse createUser(AdminCreateUserRequest req) {
        if (userRepo.existsByEmail(req.email())) {
            throw new IllegalStateException("Email already in use");
        }

        // Validate role name
        String roleName = req.role().trim().toUpperCase(); // e.g., "PLANNER"
        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + roleName));

        // Create user with that role
        User user = User.builder()
                .name(req.name())
                .email(req.email())
                .phone(req.phone())
                .passwordHash(encoder.encode(req.tempPassword()))
                .status(UserStatus.ACTIVE)
                .failedAttempts(0)
                .lockUntil(null)
                .roles(Set.of(role))
                .build();

        User saved = userRepo.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getPhone(),
                saved.getStatus(), // <-- pass UserStatus, not String
                saved.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet()),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}