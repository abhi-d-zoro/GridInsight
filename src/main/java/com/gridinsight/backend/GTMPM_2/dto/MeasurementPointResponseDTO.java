package com.gridinsight.backend.GTMPM_2.dto;

import com.gridinsight.backend.GTMPM_2.entity.AssetType;
import com.gridinsight.backend.GTMPM_2.entity.PointStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MeasurementPointResponseDTO {
    private Long id;
    private Long zoneId;
    private String zoneName;
    private AssetType assetType;
    private String identifier;
    private String unit;
    private PointStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}