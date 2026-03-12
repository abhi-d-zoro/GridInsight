package com.gridinsight.backend.GTMPM_2.dto;

import com.gridinsight.backend.GTMPM_2.entity.AssetType;
import com.gridinsight.backend.GTMPM_2.entity.PointStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MeasurementPointRequestDTO {

    @NotNull(message = "Zone ID is required")
    private Long zoneId;

    @NotNull(message = "AssetType is required")
    private AssetType assetType;

    @NotBlank(message = "Identifier is required")
    private String identifier;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Status is required")
    private PointStatus status;
}