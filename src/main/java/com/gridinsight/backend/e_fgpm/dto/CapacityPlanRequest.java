package com.gridinsight.backend.e_fgpm.dto;

public class CapacityPlanRequest {
    private String zoneId;
    private String horizon;
    private Double recommendedCapacityMw;
    private String notes;

    // Getters and Setters
    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }
    public String getHorizon() { return horizon; }
    public void setHorizon(String horizon) { this.horizon = horizon; }
    public Double getRecommendedCapacityMw() { return recommendedCapacityMw; }
    public void setRecommendedCapacityMw(Double recommendedCapacityMw) { this.recommendedCapacityMw = recommendedCapacityMw; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}