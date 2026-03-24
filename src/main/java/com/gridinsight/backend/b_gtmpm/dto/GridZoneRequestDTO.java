package com.gridinsight.backend.b_gtmpm.dto;

import com.gridinsight.backend.b_gtmpm.entity.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GridZoneRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Region is required")
    private String region;

    @NotBlank(message = "Voltage Level is required")
    private String voltageLevel;

    @NotNull(message = "Status is required")
    private Status status;
}