package com.gridinsight.backend.e_fgpm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "forecast_jobs")
public class ForecastJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String zoneId;
    private String modelVersion;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // ✅ Replace List<Double> with proper entity mapping
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ForecastHourlyResult> hourlyForecasts = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<ForecastHourlyResult> getHourlyForecasts() { return hourlyForecasts; }
    public void setHourlyForecasts(List<ForecastHourlyResult> hourlyForecasts) { this.hourlyForecasts = hourlyForecasts; }
}
