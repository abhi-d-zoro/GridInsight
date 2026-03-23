package com.gridinsight.backend.FGPM_5.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "capacity_plans")
public class CapacityPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String zoneId;

    @Column(nullable = false)
    private String horizon; // e.g., "2026-Q3" or "2027"

    @Column(nullable = false)
    private Double recommendedCapacityMw;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private Integer planVersion; // For the "Versioned" requirement

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

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