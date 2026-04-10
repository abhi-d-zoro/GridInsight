package com.gridinsight.backend.e_fgpm.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "month_forecast_records")
public class MonthForecastRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_type", nullable = false)
    private String assetType;

    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate;

    @Column(name = "forecast_value", nullable = false)
    private Double forecastValue;

    @Column(name = "lower_bound", nullable = false)
    private Double lowerBound;

    @Column(name = "upper_bound", nullable = false)
    private Double upperBound;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }

    public LocalDate getForecastDate() { return forecastDate; }
    public void setForecastDate(LocalDate forecastDate) { this.forecastDate = forecastDate; }

    public Double getForecastValue() { return forecastValue; }
    public void setForecastValue(Double forecastValue) { this.forecastValue = forecastValue; }

    public Double getLowerBound() { return lowerBound; }
    public void setLowerBound(Double lowerBound) { this.lowerBound = lowerBound; }

    public Double getUpperBound() { return upperBound; }
    public void setUpperBound(Double upperBound) { this.upperBound = upperBound; }
}
