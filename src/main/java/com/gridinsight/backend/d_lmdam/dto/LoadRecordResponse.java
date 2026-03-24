package com.gridinsight.backend.d_lmdam.dto;

import com.gridinsight.backend.d_lmdam.entity.DemandType;

import java.time.Instant;

public record LoadRecordResponse(
        Long loadId,
        Long zoneId,
        Instant timestamp,
        double demandMW,
        DemandType demandType,
        Instant createdAt,
        Instant updatedAt
) {}