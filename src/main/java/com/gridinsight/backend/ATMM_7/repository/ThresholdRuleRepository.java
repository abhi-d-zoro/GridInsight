package com.gridinsight.backend.ATMM_7.repository;

import com.gridinsight.backend.ATMM_7.entity.ThresholdRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThresholdRuleRepository extends JpaRepository<ThresholdRule, Long> {
    List<ThresholdRule> findByZoneIdAndMetricNameAndActive(Long zoneId, String metricName, boolean active);
}