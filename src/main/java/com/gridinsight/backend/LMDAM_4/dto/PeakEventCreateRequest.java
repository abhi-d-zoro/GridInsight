package com.gridinsight.backend.LMDAM_4.dto;

import com.gridinsight.backend.LMDAM_4.entity.Severity;
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