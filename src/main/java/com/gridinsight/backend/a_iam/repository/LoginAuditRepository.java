package com.gridinsight.backend.a_iam.repository;

import com.gridinsight.backend.a_iam.entity.LoginAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {
}

