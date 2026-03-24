package com.gridinsight.backend.a_iam.repository;

import com.gridinsight.backend.a_iam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByEmailOrPhone(String email, String phone);
}