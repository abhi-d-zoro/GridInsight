package com.gridinsight.backend.b_gtmpm.dto;

import com.gridinsight.backend.b_gtmpm.entity.AssetType;
import com.gridinsight.backend.b_gtmpm.entity.PointStatus;
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