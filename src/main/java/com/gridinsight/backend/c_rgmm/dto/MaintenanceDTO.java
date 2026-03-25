package com.gridinsight.backend.c_rgmm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
