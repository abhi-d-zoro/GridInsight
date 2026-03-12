package com.gridinsight.backend.LMDAM_4.dto;

import com.gridinsight.backend.LMDAM_4.entity.DemandType;

import java.time.Instant;

public record LoadRecordUpdateRequest(
        Instant timestamp,
        Double demandMW,
        DemandType demandType
) {}