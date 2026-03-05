package com.gridinsight.backend.service;

import com.gridinsight.backend.dto.CreateUserRequest;
import com.gridinsight.backend.dto.UpdateUserRequest;
import com.gridinsight.backend.dto.UserResponse;
import com.gridinsight.backend.entity.Role;
import com.gridinsight.backend.entity.User;
import com.gridinsight.backend.entity.UserStatus;
import com.gridinsight.backend.repository.RoleRepository;
import com.gridinsight.backend.repository.UserRepository;
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
    public UserResponse createUser(CreateUserRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists");
        }

        // Create user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .failedAttempts(0)
                .roles(new HashSet<>())
                .build();

        // Assign roles
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = request.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        User savedUser = userRepository.save(user);

        // Audit log
        Long actorUserId = getCurrentUserId();
        Map<String, Object> details = new HashMap<>();
        details.put("name", savedUser.getName());
        details.put("email", savedUser.getEmail());
        details.put("roles", request.getRoles());
        auditLogService.logUserCreated(actorUserId, savedUser.getId(), details);

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
        return userRepository.findAll(pageable)
                .map(this::mapToResponse);
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
                    .map(roleName -> roleRepository.findByName(roleName)
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
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                String email = authentication.getName();
                return userRepository.findByEmail(email)
                        .map(User::getId)
                        .orElse(null);
            }
        } catch (Exception e) {
            log.warn("Failed to get current user ID", e);
        }
        return null;
    }
}

