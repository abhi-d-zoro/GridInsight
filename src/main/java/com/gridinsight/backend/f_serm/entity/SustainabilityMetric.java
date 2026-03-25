package com.gridinsight.backend.f_serm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class SustainabilityMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metricId;

    private String period; // YYYY-MM
    private Double renewableSharePct;
    private Double emissionsAvoidedTons;
    private LocalDate generatedDate;

    // Getters and Setters
    public Long getMetricId() { return metricId; }
    public void setMetricId(Long metricId) { this.metricId = metricId; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Double getRenewableSharePct() { return renewableSharePct; }
    public void setRenewableSharePct(Double renewableSharePct) { this.renewableSharePct = renewableSharePct; }

    public Double getEmissionsAvoidedTons() { return emissionsAvoidedTons; }
    public void setEmissionsAvoidedTons(Double emissionsAvoidedTons) { this.emissionsAvoidedTons = emissionsAvoidedTons; }

    public LocalDate getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }
}
