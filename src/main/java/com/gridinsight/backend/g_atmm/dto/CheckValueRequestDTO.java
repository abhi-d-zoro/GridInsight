package com.gridinsight.backend.g_atmm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckValueRequestDTO {

    @NotNull
    private Long zoneId;

    private Long assetId;

    @NotBlank
    private String metricName;

    @NotNull
    private Double value;

    @NotBlank
    private String unit;
}