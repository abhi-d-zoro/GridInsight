package com.gridinsight.backend.g_atmm.repository;

import com.gridinsight.backend.g_atmm.entity.AlertActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertActivityRepository extends JpaRepository<AlertActivity, Long> {
    List<AlertActivity> findByAlertIdOrderByTimestampAsc(Long alertId);
}