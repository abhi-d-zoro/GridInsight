package com.gridinsight.backend.LMDAM_4.dto;

import com.gridinsight.backend.LMDAM_4.entity.Severity;

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