package com.gridinsight.backend.g_atmm.dto;

import com.gridinsight.backend.g_atmm.entity.ComparisonOperator;
import com.gridinsight.backend.g_atmm.entity.RuleScope;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ThresholdRuleDTO {

    private Long id;

    private String metricName;
    private RuleScope scope;

    private Long zoneId;
    private Long assetId;

    private Double thresholdValue;
    private ComparisonOperator comparison;

    private String unit;
    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}