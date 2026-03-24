package com.gridinsight.backend.d_lmdam.dto;

import com.gridinsight.backend.d_lmdam.entity.DemandType;

import java.time.Instant;

public record LoadRecordUpdateRequest(
        Instant timestamp,
        Double demandMW,
        DemandType demandType
) {}