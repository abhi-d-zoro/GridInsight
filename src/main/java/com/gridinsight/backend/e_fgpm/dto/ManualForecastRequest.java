package com.gridinsight.backend.e_fgpm.dto;

import java.time.LocalDate;
import java.util.List;

public class ManualForecastRequest {
    private String zoneId;
    private LocalDate targetDate;
    private List<HourlyForecastDTO> hourlyForecasts;

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public List<HourlyForecastDTO> getHourlyForecasts() { return hourlyForecasts; }
    public void setHourlyForecasts(List<HourlyForecastDTO> hourlyForecasts) { this.hourlyForecasts = hourlyForecasts; }
}
