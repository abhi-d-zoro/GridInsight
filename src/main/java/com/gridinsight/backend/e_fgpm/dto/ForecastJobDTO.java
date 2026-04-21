package com.gridinsight.backend.e_fgpm.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ForecastJobDTO {
    private Long id;
    private String zoneId;
    private String modelVersion;
    private String status;
    private LocalDate targetDate;
    private LocalDateTime createdAt;
    private List<HourlyForecastDTO> hourlyForecast;
}
