package com.gridinsight.backend.LMDAM_4.dto;

import com.gridinsight.backend.LMDAM_4.entity.Severity;

import java.time.Instant;

public record PeakEventUpdateRequest(
        Instant startTime,
        Instant endTime,
        Double peakMW,
        Severity severity
) {}