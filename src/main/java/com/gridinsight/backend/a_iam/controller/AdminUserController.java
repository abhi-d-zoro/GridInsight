package com.gridinsight.backend.a_iam.controller;

import com.gridinsight.backend.a_iam.dto.AdminCreateUserRequest;
import com.gridinsight.backend.a_iam.dto.UpdateUserRequest;
import com.gridinsight.backend.a_iam.dto.UserResponse;
import com.gridinsight.backend.a_iam.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserManagementService userManagementService;

    // CREATE user (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody AdminCreateUserRequest req) {
        UserResponse created = userManagementService.createUser(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // LIST users (Admin only, paginated)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(userManagementService.getAllUsers(pageable));
    }

    // GET user by id (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.getUser(id));
    }

    // UPDATE user (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(userManagementService.updateUser(id, req));
    }

    // DELETE user (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}