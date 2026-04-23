package com.gridinsight.backend.e_fgpm.dto;

import java.time.LocalDate;

public class DailyForecastDTO {
    private LocalDate date;
    private Double forecastValueMW;
    private Double confidenceIntervalLower;
    private Double confidenceIntervalUpper;

    public DailyForecastDTO() {}

    public DailyForecastDTO(LocalDate date,
                            Double forecastValueMW,
                            Double confidenceIntervalLower,
                            Double confidenceIntervalUpper) {
        this.date = date;
        this.forecastValueMW = forecastValueMW;
        this.confidenceIntervalLower = confidenceIntervalLower;
        this.confidenceIntervalUpper = confidenceIntervalUpper;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getForecastValueMW() {
        return forecastValueMW;
    }

    public void setForecastValueMW(Double forecastValueMW) {
        this.forecastValueMW = forecastValueMW;
    }

    public Double getConfidenceIntervalLower() {
        return confidenceIntervalLower;
    }

    public void setConfidenceIntervalLower(Double confidenceIntervalLower) {
        this.confidenceIntervalLower = confidenceIntervalLower;
    }

    public Double getConfidenceIntervalUpper() {
        return confidenceIntervalUpper;
    }

    public void setConfidenceIntervalUpper(Double confidenceIntervalUpper) {
        this.confidenceIntervalUpper = confidenceIntervalUpper;
    }
}
