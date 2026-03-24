package com.gridinsight.backend.d_lmdam.dto;

import com.gridinsight.backend.d_lmdam.entity.Severity;

import java.time.Instant;

public record PeakEventResponse(
        Long peakId,
        Long zoneId,
        Instant startTime,
        Instant endTime,
        double peakMW,
        Severity severity,
        Instant createdAt,
        Instant updatedAt
) {}