package com.gridinsight.backend.c_rgmm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TrendResponseDto {
    private String assetId;
    private List<TrendPointDto> points;
}
