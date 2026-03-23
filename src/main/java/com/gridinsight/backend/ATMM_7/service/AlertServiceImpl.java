package com.gridinsight.backend.ATMM_7.service;

import com.gridinsight.backend.ATMM_7.dto.CheckValueRequestDTO;
import com.gridinsight.backend.ATMM_7.entity.*;
import com.gridinsight.backend.ATMM_7.repository.AlertRepository;
import com.gridinsight.backend.ATMM_7.repository.ThresholdRuleRepository;
import com.gridinsight.backend.z_common.audit.AuditLogService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final ThresholdRuleRepository rules;
    private final AlertRepository alerts;
    private final AuditLogService audit;

    @Override
    public List<Alert> evaluate(CheckValueRequestDTO dto) {

        List<ThresholdRule> ruleList =
                rules.findByZoneIdAndMetricNameAndActive(dto.getZoneId(),
                        dto.getMetricName(), true);

        List<Alert> out = new ArrayList<>();

        for (ThresholdRule rule : ruleList) {

            if (!rule.getUnit().equalsIgnoreCase(dto.getUnit())) {
                throw new IllegalStateException("Unit mismatch: rule=" + rule.getUnit());
            }

            boolean violated = evaluateComparison(dto.getValue(), rule);

            if (violated) {
                Alert alert = buildAlert(rule, dto);
                alerts.save(alert);
                out.add(alert);

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
            }
        }
        return out;
    }

    private boolean evaluateComparison(Double value, ThresholdRule rule) {
        return switch (rule.getComparison()) {
            case GREATER_THAN -> value > rule.getThresholdValue();
            case GREATER_OR_EQUAL -> value >= rule.getThresholdValue();
            case LESS_THAN -> value < rule.getThresholdValue();
            case LESS_OR_EQUAL -> value <= rule.getThresholdValue();
        };
    }

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

    @Override
    public Alert getById(Long id) {
        return alerts.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alert not found"));
    }

    @Override
    public List<Alert> getAll() {
        return alerts.findAll();
    }
}