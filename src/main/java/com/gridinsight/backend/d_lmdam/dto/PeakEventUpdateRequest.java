package com.gridinsight.backend.d_lmdam.dto;

import com.gridinsight.backend.d_lmdam.entity.Severity;

import java.time.Instant;

public record PeakEventUpdateRequest(
        Instant startTime,
        Instant endTime,
        Double peakMW,
        Severity severity
) {}