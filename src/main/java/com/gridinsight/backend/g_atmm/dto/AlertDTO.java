package com.gridinsight.backend.g_atmm.dto;

import com.gridinsight.backend.g_atmm.entity.AlertStatus;
import com.gridinsight.backend.g_atmm.entity.ComparisonOperator;
import com.gridinsight.backend.g_atmm.entity.Severity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AlertDTO {

    private Long id;

    private Long ruleId;
    private Long zoneId;
    private Long assetId;

    private String metricName;
    private Double actualValue;
    private Double thresholdValue;

    private ComparisonOperator comparison;
    private Severity severity;
    private AlertStatus status;

    private String message;
    private String correlationId;

    private LocalDateTime acknowledgedAt;
    private String acknowledgedBy;

    private LocalDateTime closedAt;
    private String resolutionNote;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}