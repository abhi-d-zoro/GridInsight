package com.gridinsight.backend.RGMM_3.dto;

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
