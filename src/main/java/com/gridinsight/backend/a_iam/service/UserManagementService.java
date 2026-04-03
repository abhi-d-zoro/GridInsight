package com.gridinsight.backend.a_iam.service;

import com.gridinsight.backend.a_iam.dto.AdminCreateUserRequest;
import com.gridinsight.backend.a_iam.dto.UpdateUserRequest;
import com.gridinsight.backend.a_iam.dto.UserResponse;
import com.gridinsight.backend.a_iam.entity.Role;
import com.gridinsight.backend.a_iam.entity.User;
import com.gridinsight.backend.a_iam.entity.UserStatus;
import com.gridinsight.backend.a_iam.repository.RoleRepository;
import com.gridinsight.backend.a_iam.repository.UserRepository;
import com.gridinsight.backend.z_common.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    // ----------------------------------------------------------------
    // CREATE USER (Admin only)
    // ----------------------------------------------------------------
    @Transactional
    public UserResponse createUser(AdminCreateUserRequest request) {

        // 1️⃣ Email uniqueness
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException(
                    "User with email " + request.email() + " already exists"
            );
        }

        // 2️⃣ Resolve SINGLE role
        String roleName = java.util.Optional.ofNullable(request.role())
                .map(r -> r.trim().toUpperCase())
                .filter(r -> !r.isBlank())
                .orElse("GRID_ANALYST"); // least-privilege default

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() ->
                        new IllegalArgumentException("Role not found: " + roleName)
                );

        // 3️⃣ Create user
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .passwordHash(passwordEncoder.encode(request.tempPassword()))
                .status(UserStatus.ACTIVE)
                .failedAttempts(0)
                .role(role) // ✅ SINGLE ROLE
                .build();

        User savedUser = userRepository.save(user);

        // 4️⃣ Audit log
        Long actorUserId = getCurrentUserId();
        Map<String, Object> details = new HashMap<>();
        details.put("name", savedUser.getName());
        details.put("email", savedUser.getEmail());
        details.put("role", role.getName());

        auditLogService.logUserCreated(actorUserId, savedUser.getId(), details);

        // 5️⃣ Return response
        return mapToResponse(savedUser);
    }

    // ----------------------------------------------------------------
    // GET USER BY ID
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found with id: " + id)
                );
        return mapToResponse(user);
    }

    // ----------------------------------------------------------------
    // LIST / SEARCH USERS (name or email)
    // ----------------------------------------------------------------
        @Transactional(readOnly = true)
        public Page<UserResponse> listUsers(String search, Pageable pageable) {

            // ✅ Case 1: No search → normal paginated list
            if (search == null || search.trim().isEmpty()) {
                return userRepository.findAll(pageable)
                        .map(this::mapToResponse);
            }

            // ✅ Case 2: Search by name OR email
            return userRepository
                    .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            search,
                            search,
                            pageable
                    )
                    .map(this::mapToResponse);
        }

    // ----------------------------------------------------------------
    // UPDATE USER (Partial Update)
    // ----------------------------------------------------------------
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found with id: " + id)
                );

        Map<String, Object> changedFields = new HashMap<>();

        // ---- Name ----
        if (request.getName() != null &&
                !request.getName().equals(user.getName())) {
            changedFields.put("name",
                    Map.of("old", user.getName(), "new", request.getName()));
            user.setName(request.getName());
        }

        // ---- Email ----
        if (request.getEmail() != null &&
                !request.getEmail().equals(user.getEmail())) {

            userRepository.findByEmail(request.getEmail())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new IllegalArgumentException("Email already in use");
                        }
                    });

            changedFields.put("email",
                    Map.of("old", user.getEmail(), "new", request.getEmail()));
            user.setEmail(request.getEmail());
        }

        // ---- Phone ----
        if (request.getPhone() != null &&
                !Objects.equals(request.getPhone(), user.getPhone())) {
            changedFields.put("phone",
                    Map.of("old", user.getPhone(), "new", request.getPhone()));
            user.setPhone(request.getPhone());
        }

        // ---- Password ----
        if (request.getPassword() != null &&
                !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            changedFields.put("password",
                    Map.of("old", "***", "new", "***"));
        }

        // ---- Status ----
        if (request.getStatus() != null) {
            UserStatus newStatus =
                    UserStatus.valueOf(request.getStatus().toUpperCase());

            if (newStatus != user.getStatus()) {
                changedFields.put("status",
                        Map.of("old", user.getStatus(), "new", newStatus));
                user.setStatus(newStatus);
            }
        }

        // ✅ SINGLE ROLE UPDATE
        if (request.getRole() != null &&
                !request.getRole().isBlank()) {

            String roleName = request.getRole().trim().toUpperCase();
            Role newRole = roleRepository.findByName(roleName)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Role not found: " + roleName)
                    );

            if (user.getRole() == null ||
                    !user.getRole().getName().equals(newRole.getName())) {

                changedFields.put("role",
                        Map.of(
                                "old", user.getRole() != null ? user.getRole().getName() : null,
                                "new", newRole.getName()
                        ));

                user.setRole(newRole);
            }
        }

        User updatedUser = userRepository.save(user);

        // ---- Audit if changes happened ----
        if (!changedFields.isEmpty()) {
            Long actorUserId = getCurrentUserId();
            auditLogService.logUserUpdated(
                    actorUserId,
                    updatedUser.getId(),
                    changedFields
            );
        }

        return mapToResponse(updatedUser);
    }

    // ----------------------------------------------------------------
    // DELETE USER
    // ----------------------------------------------------------------
    @Transactional
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found with id: " + id)
                );

        Long actorUserId = getCurrentUserId();

        Map<String, Object> details = new HashMap<>();
        details.put("name", user.getName());
        details.put("email", user.getEmail());
        details.put("role",
                user.getRole() != null ? user.getRole().getName() : null);

        auditLogService.logUserDeleted(actorUserId, user.getId(), details);
        userRepository.delete(user);
    }

    // ----------------------------------------------------------------
    // MAP ENTITY → RESPONSE
    // ----------------------------------------------------------------
    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                user.getRole() != null ? user.getRole().getName() : null,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    // ----------------------------------------------------------------
    // CURRENT USER FROM SECURITY CONTEXT
    // ----------------------------------------------------------------
    private Long getCurrentUserId() {
        try {
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null &&
                    authentication.isAuthenticated() &&
                    !"anonymousUser".equals(authentication.getPrincipal())) {

                Object principal = authentication.getPrincipal();

                if (principal instanceof User u) {
                    return u.getId();
                }

                if (principal instanceof String email) {
                    return userRepository.findByEmail(email)
                            .map(User::getId)
                            .orElse(null);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get current user ID", e);
        }
        return null;
    }
}
