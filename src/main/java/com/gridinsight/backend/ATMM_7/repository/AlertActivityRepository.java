package com.gridinsight.backend.ATMM_7.repository;

import com.gridinsight.backend.ATMM_7.entity.AlertActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertActivityRepository extends JpaRepository<AlertActivity, Long> {
    List<AlertActivity> findByAlertIdOrderByTimestampAsc(Long alertId);
}