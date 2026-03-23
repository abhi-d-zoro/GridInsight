package com.gridinsight.backend.ATMM_7.dto;

import jakarta.validation.constraints.*;
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