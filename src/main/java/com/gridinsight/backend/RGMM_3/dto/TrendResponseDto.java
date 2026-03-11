package com.gridinsight.backend.RGMM_3.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TrendResponseDto {
    private String assetId;
    private List<TrendPointDto> points;
}
