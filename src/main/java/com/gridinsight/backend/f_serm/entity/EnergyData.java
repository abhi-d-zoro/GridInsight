package com.gridinsight.backend.f_serm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "energy_data")
public class EnergyData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String period; // e.g. "2026-01"
    private Double totalGeneration; // total energy generated (MWh)
    private Double renewableGeneration; // renewable energy generated (MWh)
    private Double emissionsAvoided; // emissions avoided (tons CO2)

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Double getTotalGeneration() { return totalGeneration; }
    public void setTotalGeneration(Double totalGeneration) { this.totalGeneration = totalGeneration; }

    public Double getRenewableGeneration() { return renewableGeneration; }
    public void setRenewableGeneration(Double renewableGeneration) { this.renewableGeneration = renewableGeneration; }

    public Double getEmissionsAvoided() { return emissionsAvoided; }
    public void setEmissionsAvoided(Double emissionsAvoided) { this.emissionsAvoided = emissionsAvoided; }
}
