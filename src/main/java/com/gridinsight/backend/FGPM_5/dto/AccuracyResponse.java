package com.gridinsight.backend.FGPM_5.dto;

import java.util.List;

public class AccuracyResponse {
    private String zoneId;
    private String targetDate;
    private double overallMape; // Mean Absolute Percentage Error
    private List<HourlyComparison> hourlyData;

    // Getters and Setters
    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }
    public String getTargetDate() { return targetDate; }
    public void setTargetDate(String targetDate) { this.targetDate = targetDate; }
    public double getOverallMape() { return overallMape; }
    public void setOverallMape(double overallMape) { this.overallMape = overallMape; }
    public List<HourlyComparison> getHourlyData() { return hourlyData; }
    public void setHourlyData(List<HourlyComparison> hourlyData) { this.hourlyData = hourlyData; }
}