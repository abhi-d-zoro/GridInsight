package com.gridinsight.backend.RGMM_3.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceDTO {
    private String note;
    private LocalDate startDate;
    private LocalDate endDate;
}
