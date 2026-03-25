package com.gridinsight.backend.e_fgpm.dto;

import java.util.List;

public class AccuracyResponse {

    private String zoneId;
    private String targetDate;
    private double overallMape;
    private List<HourlyComparison> hourlyData;

    public AccuracyResponse() {}

    public AccuracyResponse(String zoneId,
                            String targetDate,
                            double overallMape,
                            List<HourlyComparison> hourlyData) {
        this.zoneId = zoneId;
        this.targetDate = targetDate;
        this.overallMape = overallMape;
        this.hourlyData = hourlyData;
    }

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }

    public String getTargetDate() { return targetDate; }
    public void setTargetDate(String targetDate) { this.targetDate = targetDate; }

    public double getOverallMape() { return overallMape; }
    public void setOverallMape(double overallMape) { this.overallMape = overallMape; }

    public List<HourlyComparison> getHourlyData() { return hourlyData; }
    public void setHourlyData(List<HourlyComparison> hourlyData) { this.hourlyData = hourlyData; }
}