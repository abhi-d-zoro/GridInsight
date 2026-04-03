package com.gridinsight.backend.a_iam.service;

import com.gridinsight.backend.a_iam.dto.AdminCreateUserRequest;
import com.gridinsight.backend.a_iam.dto.UserResponse;
import com.gridinsight.backend.a_iam.entity.Role;
import com.gridinsight.backend.a_iam.entity.User;
import com.gridinsight.backend.a_iam.entity.UserStatus;
import com.gridinsight.backend.a_iam.repository.RoleRepository;
import com.gridinsight.backend.a_iam.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    @Transactional
    public UserResponse createUser(AdminCreateUserRequest req) {

        // 1️⃣ Email uniqueness check
        if (userRepo.existsByEmail(req.email())) {
            throw new IllegalStateException("Email already in use");
        }

        // 2️⃣ Resolve SINGLE role
        String roleName = req.role()
                .trim()
                .toUpperCase(); // e.g. "ADMIN", "ESG", "PLANNER"

        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() ->
                        new IllegalArgumentException("Unknown role: " + roleName)
                );

        // 3️⃣ Create user with ONE role
        User user = User.builder()
                .name(req.name())
                .email(req.email())
                .phone(req.phone())
                .passwordHash(encoder.encode(req.tempPassword()))
                .status(UserStatus.ACTIVE)
                .failedAttempts(0)
                .lockUntil(null)
                .role(role) // ✅ SINGLE ROLE
                .build();

        User saved = userRepo.save(user);

        // 4️⃣ Map to response (single role)
        return new UserResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getPhone(),
                saved.getStatus(),
                saved.getRole().getName(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}