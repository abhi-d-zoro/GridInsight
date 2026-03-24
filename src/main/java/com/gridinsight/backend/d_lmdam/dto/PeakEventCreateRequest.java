package com.gridinsight.backend.d_lmdam.dto;

import com.gridinsight.backend.d_lmdam.entity.Severity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;

public record PeakEventCreateRequest(
        @NotNull Long zoneId,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        @PositiveOrZero double peakMW,
        @NotNull Severity severity
) {}