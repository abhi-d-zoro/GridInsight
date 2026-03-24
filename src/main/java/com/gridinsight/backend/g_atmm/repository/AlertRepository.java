package com.gridinsight.backend.g_atmm.repository;

import com.gridinsight.backend.g_atmm.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
}