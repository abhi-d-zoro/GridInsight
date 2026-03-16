package com.gridinsight.backend.FGPM_5.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
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
    private String status; // e.g., PENDING, COMPLETED, FAILED

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime targetDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // Stores the 24-hour forecast results simple list of doubles for this example
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "forecast_hourly_results", joinColumns = @JoinColumn(name = "job_id"))
    @OrderColumn(name = "hour_index")
    @Column(name = "load_value")
    private List<Double> hourlyForecast = new ArrayList<>();

    public ForecastJob() {
    }

    @PrePersist
    private void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDateTime targetDate) {
        this.targetDate = targetDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Double> getHourlyForecast() {
        return hourlyForecast;
    }

    public void setHourlyForecast(List<Double> hourlyForecast) {
        this.hourlyForecast = hourlyForecast;
    }
}