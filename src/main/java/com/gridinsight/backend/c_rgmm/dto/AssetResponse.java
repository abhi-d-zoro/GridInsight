package com.gridinsight.backend.c_rgmm.dto;

import com.gridinsight.backend.c_rgmm.entity.AssetStatus;
import com.gridinsight.backend.c_rgmm.entity.AssetType;

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
