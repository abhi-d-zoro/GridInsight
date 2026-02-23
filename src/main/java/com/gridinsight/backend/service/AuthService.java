package com.gridinsight.backend.service;

import com.gridinsight.backend.dto.RegisterRequest;
import com.gridinsight.backend.entity.Role;
import com.gridinsight.backend.entity.User;
import com.gridinsight.backend.entity.UserStatus;
import com.gridinsight.backend.repository.RoleRepository;
import com.gridinsight.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    @Transactional
    public void register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("Email already in use");
        }

        Set<Role> roles = req.getRoles().stream()
                .map(roleName -> roleRepo.findByName(roleName)
                        .orElseThrow(() -> new NoSuchElementException("Role " + roleName + " not found")))
                .collect(Collectors.toSet());

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .passwordHash(encoder.encode(req.getPassword()))
                .status(UserStatus.ACTIVE)
                .roles(roles)
                .build();

        userRepo.save(user);
    }
}