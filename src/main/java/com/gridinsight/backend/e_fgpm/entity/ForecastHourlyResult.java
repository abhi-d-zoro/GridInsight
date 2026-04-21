package com.gridinsight.backend.e_fgpm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "forecast_hourly_results")
public class ForecastHourlyResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int hour;

    @Column(name = "forecast_value")
    private double forecastValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private ForecastJob job;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public double getForecastValue() { return forecastValue; }
    public void setForecastValue(double forecastValue) { this.forecastValue = forecastValue; }

    public ForecastJob getJob() { return job; }
    public void setJob(ForecastJob job) { this.job = job; }
}
