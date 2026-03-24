package com.gridinsight.backend.g_atmm.service;

import com.gridinsight.backend.g_atmm.dto.ThresholdRuleRequestDTO;
import com.gridinsight.backend.g_atmm.entity.ThresholdRule;
import com.gridinsight.backend.g_atmm.repository.ThresholdRuleRepository;
import com.gridinsight.backend.z_common.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ThresholdRuleServiceImpl implements ThresholdRuleService {

    private final ThresholdRuleRepository repository;
    private final AuditLogService audit;

    @Override
    public ThresholdRule create(ThresholdRuleRequestDTO r) {

        ThresholdRule rule = ThresholdRule.builder()
                .metricName(r.getMetricName())
                .scope(r.getScope())
                .zoneId(r.getZoneId())
                .assetId(r.getAssetId())
                .thresholdValue(r.getThresholdValue())
                .comparison(r.getComparison())
                .unit(r.getUnit())
                .active(r.isActive())
                .build();

        ThresholdRule saved = repository.save(rule);

        audit.logAction(
                "THRESHOLD_RULE_CREATED",
                null,
                saved.getId(),
                "ThresholdRule",
                Map.of("ruleId", saved.getId())
        );

        return saved;
    }

    @Override
    public ThresholdRule update(Long id, ThresholdRuleRequestDTO r) {

        ThresholdRule rule = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rule not found"));

        rule.setMetricName(r.getMetricName());
        rule.setScope(r.getScope());
        rule.setZoneId(r.getZoneId());
        rule.setAssetId(r.getAssetId());
        rule.setThresholdValue(r.getThresholdValue());
        rule.setComparison(r.getComparison());
        rule.setUnit(r.getUnit());
        rule.setActive(r.isActive());

        ThresholdRule updated = repository.save(rule);

        audit.logAction(
                "THRESHOLD_RULE_UPDATED",
                null,
                id,
                "ThresholdRule",
                Map.of("ruleId", id)
        );

        return updated;
    }

    @Override
    public void delete(Long id) {

        ThresholdRule rule = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rule not found"));

        repository.delete(rule);

        audit.logAction(
                "THRESHOLD_RULE_DELETED",
                null,
                id,
                "ThresholdRule",
                Map.of("ruleId", id)
        );
    }

    @Override
    public ThresholdRule getById(Long id) {

        ThresholdRule rule = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rule not found"));

        audit.logAction(
                "THRESHOLD_RULE_READ",
                null,
                id,
                "ThresholdRule",
                Map.of("ruleId", id)
        );

        return rule;
    }

    @Override
    public List<ThresholdRule> getAll() {

        audit.logAction(
                "THRESHOLD_RULE_LIST",
                null,
                null,
                "ThresholdRule",
                Map.of("scope", "ALL")
        );

        return repository.findAll();
    }
}