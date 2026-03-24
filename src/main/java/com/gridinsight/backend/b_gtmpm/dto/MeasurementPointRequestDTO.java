package com.gridinsight.backend.b_gtmpm.dto;

import com.gridinsight.backend.b_gtmpm.entity.AssetType;
import com.gridinsight.backend.b_gtmpm.entity.PointStatus;
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