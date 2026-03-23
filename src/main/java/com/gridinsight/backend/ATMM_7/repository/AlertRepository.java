package com.gridinsight.backend.ATMM_7.repository;

import com.gridinsight.backend.ATMM_7.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
}