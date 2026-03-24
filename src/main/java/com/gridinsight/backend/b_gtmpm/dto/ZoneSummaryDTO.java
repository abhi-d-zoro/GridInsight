package com.gridinsight.backend.b_gtmpm.dto;

import com.gridinsight.backend.b_gtmpm.entity.Status;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ZoneSummaryDTO {
    private Long id;
    private String name;
    private String region;
    private String voltageLevel;
    private Status status;
    private long pointsCount;
}