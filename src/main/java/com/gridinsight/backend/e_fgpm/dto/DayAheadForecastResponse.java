package com.gridinsight.backend.e_fgpm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class DayAheadForecastResponse {
    private String zoneId;
    private LocalDate date;
    private List<HourlyForecastDTO> hourlyData;
}
