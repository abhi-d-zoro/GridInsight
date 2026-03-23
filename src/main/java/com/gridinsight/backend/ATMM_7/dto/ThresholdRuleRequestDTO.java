package com.gridinsight.backend.ATMM_7.dto;

import com.gridinsight.backend.ATMM_7.entity.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ThresholdRuleRequestDTO {

    @NotBlank
    private String metricName;

    @NotNull
    private RuleScope scope;

    private Long zoneId;
    private Long assetId;

    @NotNull
    private Double thresholdValue;

    @NotNull
    private ComparisonOperator comparison;

    @NotBlank
    private String unit;

    private boolean active;
}