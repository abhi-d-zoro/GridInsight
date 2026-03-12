package com.gridinsight.backend.LMDAM_4.dto;

import com.gridinsight.backend.LMDAM_4.entity.DemandType;

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