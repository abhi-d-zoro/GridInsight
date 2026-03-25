package com.gridinsight.backend.g_atmm.service;

import com.gridinsight.backend.g_atmm.dto.AlertDTO;
import com.gridinsight.backend.g_atmm.dto.CheckValueRequestDTO;
import com.gridinsight.backend.g_atmm.entity.Alert;
import com.gridinsight.backend.g_atmm.entity.Severity;
import com.gridinsight.backend.g_atmm.entity.ThresholdRule;
import com.gridinsight.backend.g_atmm.repository.AlertRepository;
import com.gridinsight.backend.g_atmm.repository.ThresholdRuleRepository;
import com.gridinsight.backend.z_common.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final ThresholdRuleRepository rules;
    private final AlertRepository alerts;
    private final AuditLogService audit;

    // ✅ EVALUATE & RETURN DTO LIST (NOT ENTITIES)
    public List<AlertDTO> evaluate(CheckValueRequestDTO dto) {

        List<ThresholdRule> ruleList =
                rules.findByZoneIdAndMetricNameAndActive(dto.getZoneId(),
                        dto.getMetricName(), true);

        List<AlertDTO> out = new ArrayList<>();

        for (ThresholdRule rule : ruleList) {

            if (!rule.getUnit().equalsIgnoreCase(dto.getUnit())) {
                throw new IllegalStateException("Unit mismatch: rule=" + rule.getUnit());
            }

            boolean violated = evaluateComparison(dto.getValue(), rule);

            if (violated) {
                Alert alert = buildAlert(rule, dto);
                alerts.save(alert);

                audit.logAction(
                        "ALERT_CREATED",
                        null,
                        alert.getId(),
                        "Alert",
                        Map.of(
                                "metric", alert.getMetricName(),
                                "value", alert.getActualValue()
                        )
                );

                out.add(toDTO(alert));
            }
        }
        return out;
    }

    // ✅ COMPARISON LOGIC
    private boolean evaluateComparison(Double value, ThresholdRule rule) {
        return switch (rule.getComparison()) {
            case GREATER_THAN -> value > rule.getThresholdValue();
            case GREATER_OR_EQUAL -> value >= rule.getThresholdValue();
            case LESS_THAN -> value < rule.getThresholdValue();
            case LESS_OR_EQUAL -> value <= rule.getThresholdValue();
        };
    }

    // ✅ BUILD NEW ALERT ENTITY
    private Alert buildAlert(ThresholdRule rule, CheckValueRequestDTO dto) {

        double deviation = Math.abs(dto.getValue() - rule.getThresholdValue());
        Severity severity = deviation > 50 ? Severity.HIGH :
                deviation > 20 ? Severity.MEDIUM :
                        Severity.LOW;

        return Alert.builder()
                .ruleId(rule.getId())
                .zoneId(dto.getZoneId())
                .assetId(dto.getAssetId())
                .metricName(rule.getMetricName())
                .actualValue(dto.getValue())
                .thresholdValue(rule.getThresholdValue())
                .comparison(rule.getComparison())
                .severity(severity)
                .message("Threshold violated for " + rule.getMetricName())
                .correlationId(UUID.randomUUID().toString())
                .build();
    }

    // ✅ GET ALERT BY ID (DTO)
    public AlertDTO getById(Long id) {
        Alert alert = alerts.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alert not found"));

        return toDTO(alert);
    }

    // ✅ GET ALL ALERTS (DTO)
    public List<AlertDTO> getAll() {
        return alerts.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    // ✅ ENTITY → DTO MAPPER
    private AlertDTO toDTO(Alert alert) {
        return AlertDTO.builder()
                .id(alert.getId())
                .ruleId(alert.getRuleId())
                .zoneId(alert.getZoneId())
                .assetId(alert.getAssetId())
                .metricName(alert.getMetricName())
                .actualValue(alert.getActualValue())
                .thresholdValue(alert.getThresholdValue())
                .comparison(alert.getComparison())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .message(alert.getMessage())
                .correlationId(alert.getCorrelationId())
                .acknowledgedAt(alert.getAcknowledgedAt())
                .acknowledgedBy(alert.getAcknowledgedBy())
                .closedAt(alert.getClosedAt())
                .resolutionNote(alert.getResolutionNote())
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .build();
    }
}