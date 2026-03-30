package com.gridinsight.backend.e_fgpm.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ForecastJobDTO {

    private Long id;
    private Long zoneId;
    private String modelVersion;
    private String status;
    private LocalDateTime targetDate;
    private LocalDateTime createdAt;
    private List<Double> hourlyForecast;
}