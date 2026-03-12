package com.gridinsight.backend.RGMM_3.dto;

import com.gridinsight.backend.RGMM_3.entity.AssetStatus;
import com.gridinsight.backend.RGMM_3.entity.AssetType;

import java.time.Instant;
import java.time.LocalDate;

public record AssetResponse(
        Long id,
        AssetType type,
        String location,
        String identifier,
        Double capacity,
        LocalDate commissionDate,
        AssetStatus status,
        Instant createdAt,
        Instant updatedAt,
        String maintenanceNote,
        LocalDate maintenanceStart,
        LocalDate maintenanceEnd
) {}
