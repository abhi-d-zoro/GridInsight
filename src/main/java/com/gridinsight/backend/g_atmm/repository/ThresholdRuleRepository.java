package com.gridinsight.backend.g_atmm.repository;

import com.gridinsight.backend.g_atmm.entity.ThresholdRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThresholdRuleRepository extends JpaRepository<ThresholdRule, Long> {
    List<ThresholdRule> findByZoneIdAndMetricNameAndActive(Long zoneId, String metricName, boolean active);
}