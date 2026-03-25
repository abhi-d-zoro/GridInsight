package com.gridinsight.backend.f_serm.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SustainabilityMetricDTO {

    private Long metricId;
    private String period;               // YYYY-MM
    private Double renewableSharePct;
    private Double emissionsAvoidedTons;
    private LocalDate generatedDate;
}