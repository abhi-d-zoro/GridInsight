package com.gridinsight.backend.GTMPM_2.dto;

import com.gridinsight.backend.GTMPM_2.entity.Status;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GridZoneResponseDTO {
    private Long id;
    private String name;
    private String region;
    private String voltageLevel;
    private Status status;
}