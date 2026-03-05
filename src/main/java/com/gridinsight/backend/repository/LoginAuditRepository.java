package com.gridinsight.backend.repository;

import com.gridinsight.backend.entity.LoginAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {
}

