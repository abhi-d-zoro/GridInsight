package com.gridinsight.backend.e_fgpm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HourlyForecastDTO {
    private int hour;
    private double forecastValueMW;
    private double actualValueMW;
}
