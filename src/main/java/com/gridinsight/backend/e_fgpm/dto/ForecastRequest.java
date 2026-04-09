package com.gridinsight.backend.e_fgpm.dto;

import java.time.LocalDate;

public class ForecastRequest {
    private String zoneId;
    private LocalDate targetDate;

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
}
