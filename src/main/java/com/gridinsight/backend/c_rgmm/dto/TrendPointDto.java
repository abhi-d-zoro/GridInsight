package com.gridinsight.backend.c_rgmm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TrendPointDto {
    private LocalDateTime timestamp;
    private Double energy;
    private Double availability;
}
