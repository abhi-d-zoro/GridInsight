package com.gridinsight.backend.g_atmm.service;

import com.gridinsight.backend.g_atmm.dto.ThresholdRuleDTO;
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
public class ThresholdRuleService {

    private final ThresholdRuleRepository repository;
    private final AuditLogService audit;

    // ✅ CREATE rule → returns DTO
    public ThresholdRuleDTO create(ThresholdRuleRequestDTO r) {

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

        return toDTO(saved);
    }

    // ✅ UPDATE rule → returns DTO
    public ThresholdRuleDTO update(Long id, ThresholdRuleRequestDTO r) {

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

        return toDTO(updated);
    }

    // ✅ DELETE rule
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

    // ✅ GET single rule → returns DTO
    public ThresholdRuleDTO getById(Long id) {

        ThresholdRule rule = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rule not found"));

        audit.logAction(
                "THRESHOLD_RULE_READ",
                null,
                id,
                "ThresholdRule",
                Map.of("ruleId", id)
        );

        return toDTO(rule);
    }

    // ✅ GET all → returns List<DTO>
    public List<ThresholdRuleDTO> getAll() {

        audit.logAction(
                "THRESHOLD_RULE_LIST",
                null,
                null,
                "ThresholdRule",
                Map.of("scope", "ALL")
        );

        return repository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    // ✅ ENTITY ➝ DTO MAPPER
    private ThresholdRuleDTO toDTO(ThresholdRule rule) {
        return ThresholdRuleDTO.builder()
                .id(rule.getId())
                .metricName(rule.getMetricName())
                .scope(rule.getScope())
                .zoneId(rule.getZoneId())
                .assetId(rule.getAssetId())
                .thresholdValue(rule.getThresholdValue())
                .comparison(rule.getComparison())
                .unit(rule.getUnit())
                .active(rule.isActive())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}