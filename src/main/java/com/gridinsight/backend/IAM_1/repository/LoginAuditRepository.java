package com.gridinsight.backend.IAM_1.repository;

import com.gridinsight.backend.IAM_1.entity.LoginAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {
}

