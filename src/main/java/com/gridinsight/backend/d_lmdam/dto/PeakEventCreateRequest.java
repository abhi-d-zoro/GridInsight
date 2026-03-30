package com.gridinsight.backend.d_lmdam.dto;

import com.gridinsight.backend.d_lmdam.entity.Severity;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record PeakEventCreateRequest(
        @NotNull String zoneId,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        @NotNull Double peakMW,
        @NotNull Severity severity
) {}