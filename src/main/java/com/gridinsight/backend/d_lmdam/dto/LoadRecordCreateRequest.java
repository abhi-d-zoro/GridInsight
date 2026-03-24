package com.gridinsight.backend.d_lmdam.dto;

import com.gridinsight.backend.d_lmdam.entity.DemandType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;

public record LoadRecordCreateRequest(
        @NotNull Long zoneId,
        @NotNull Instant timestamp,
        @PositiveOrZero double demandMW,
        @NotNull DemandType demandType
) {}