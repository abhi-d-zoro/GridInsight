package com.gridinsight.backend.e_fgpm.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CapacityPlanDTO {

    private Long id;
    private String zoneId;
    private String horizon;
    private Double recommendedCapacityMw;
    private String notes;
    private Integer planVersion;
    private LocalDateTime createdAt;
}