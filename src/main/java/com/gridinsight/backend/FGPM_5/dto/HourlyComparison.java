package com.gridinsight.backend.FGPM_5.dto;

public class HourlyComparison {
    private int hour;
    private double actualLoad;
    private double forecastLoad;
    private double errorPercentage;

    // Constructors, Getters, and Setters
    public HourlyComparison(int hour, double actualLoad, double forecastLoad, double errorPercentage) {
        this.hour = hour;
        this.actualLoad = actualLoad;
        this.forecastLoad = forecastLoad;
        this.errorPercentage = errorPercentage;
    }

    public int getHour() { return hour; }
    public double getActualLoad() { return actualLoad; }
    public double getForecastLoad() { return forecastLoad; }
    public double getErrorPercentage() { return errorPercentage; }
}