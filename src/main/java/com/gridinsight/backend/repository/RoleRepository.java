package com.gridinsight.backend.repository;

import com.gridinsight.backend.entity.Role;
import com.gridinsight.backend.entity.Role.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}