package com.gridinsight.backend.e_fgpm.dto;

import java.time.LocalDateTime;
public class CapacityPlanDTO {
    private Long id;
    private String zoneId;
    private String horizon;
    private Double recommendedCapacityMw;
    private String notes;
    private Integer planVersion;
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }

    public String getHorizon() { return horizon; }
    public void setHorizon(String horizon) { this.horizon = horizon; }

    public Double getRecommendedCapacityMw() { return recommendedCapacityMw; }
    public void setRecommendedCapacityMw(Double recommendedCapacityMw) { this.recommendedCapacityMw = recommendedCapacityMw; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Integer getPlanVersion() { return planVersion; }
    public void setPlanVersion(Integer planVersion) { this.planVersion = planVersion; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
