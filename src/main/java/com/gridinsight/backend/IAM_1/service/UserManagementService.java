package com.gridinsight.backend.IAM_1.service;

import com.gridinsight.backend.IAM_1.dto.AdminCreateUserRequest;
import com.gridinsight.backend.IAM_1.dto.UpdateUserRequest;
import com.gridinsight.backend.IAM_1.dto.UserResponse;
import com.gridinsight.backend.IAM_1.entity.Role;
import com.gridinsight.backend.IAM_1.entity.User;
import com.gridinsight.backend.IAM_1.entity.UserStatus;
import com.gridinsight.backend.IAM_1.repository.RoleRepository;
import com.gridinsight.backend.IAM_1.repository.UserRepository;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional
    public UserResponse createUser(AdminCreateUserRequest request) {
        // 1) Email uniqueness
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("User with email " + request.email() + " already exists");
        }

        // 2) Resolve the single role from DTO (PASTE THIS BLOCK HERE)
        // --- begin pasted/updated block ---
        String roleName = java.util.Optional.ofNullable(request.role())
                .map(r -> r.trim().toUpperCase())
                .filter(r -> !r.isBlank())
                .orElse("GRIDANALYST"); // least-privilege default

        java.util.Set<String> allowed = java.util.Set.of("GRID_ANALYST", "ASSET_MANAGER", "PLANNER", "ESG", "ADMIN");
        if (!allowed.contains(roleName)) {
            throw new IllegalArgumentException("Role not allowed: " + roleName);
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        // --- end pasted/updated block ---

        // 3) Create user (record accessors: name(), email(), phone(), tempPassword())
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .passwordHash(passwordEncoder.encode(request.tempPassword()))
                .status(UserStatus.ACTIVE)
                .failedAttempts(0)
                .roles(new java.util.HashSet<>(java.util.Set.of(role))) // single role
                .build();

        User savedUser = userRepository.save(user);

        // 4) Audit
        Long actorUserId = getCurrentUserId();
        java.util.Map<String, Object> details = new java.util.HashMap<>();
        details.put("name", savedUser.getName());
        details.put("email", savedUser.getEmail());
        details.put("roles", java.util.Set.of(role.getName())); // single role we assigned
        auditLogService.logUserCreated(actorUserId, savedUser.getId(), details);

        // 5) Return response
        return mapToResponse(savedUser);
    }


    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        Map<String, Object> changedFields = new HashMap<>();

        // Track changes
        if (request.getName() != null && !request.getName().equals(user.getName())) {
            changedFields.put("name", Map.of("old", user.getName(), "new", request.getName()));
            user.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Check if new email is already taken
            userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(id)) {
                    throw new IllegalArgumentException("Email already in use");
                }
            });
            changedFields.put("email", Map.of("old", user.getEmail(), "new", request.getEmail()));
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null && !Objects.equals(request.getPhone(), user.getPhone())) {
            changedFields.put("phone", Map.of("old", user.getPhone(), "new", request.getPhone()));
            user.setPhone(request.getPhone());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            changedFields.put("password", Map.of("old", "***", "new", "***"));
        }

        if (request.getStatus() != null) {
            UserStatus newStatus = UserStatus.valueOf(request.getStatus().toUpperCase());
            if (newStatus != user.getStatus()) {
                changedFields.put("status", Map.of("old", user.getStatus(), "new", newStatus));
                user.setStatus(newStatus);
            }
        }

        if (request.getRoles() != null) {
            Set<String> oldRoles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            Set<Role> newRoles = request.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName.toUpperCase().trim())
                            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());

            if (!oldRoles.equals(request.getRoles())) {
                changedFields.put("roles", Map.of("old", oldRoles, "new", request.getRoles()));
                user.setRoles(newRoles);
            }
        }

        User updatedUser = userRepository.save(user);

        // Audit log if there were changes
        if (!changedFields.isEmpty()) {
            Long actorUserId = getCurrentUserId();
            auditLogService.logUserUpdated(actorUserId, updatedUser.getId(), changedFields);
        }

        return mapToResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Audit log before deletion
        Long actorUserId = getCurrentUserId();
        Map<String, Object> details = new HashMap<>();
        details.put("name", user.getName());
        details.put("email", user.getEmail());
        auditLogService.logUserDeleted(actorUserId, user.getId(), details);

        userRepository.delete(user);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {

                Object principal = authentication.getPrincipal();
                if (principal instanceof User u) {
                    return u.getId(); // Our JwtAuthFilter sets principal = User
                }

                // Fallback: if principal is a String (email/username)
                if (principal instanceof String email) {
                    return userRepository.findByEmail(email).map(User::getId).orElse(null);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get current user ID", e);
        }
        return null;
    }
}