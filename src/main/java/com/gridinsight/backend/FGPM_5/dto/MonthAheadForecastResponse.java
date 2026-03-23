package com.gridinsight.backend.FGPM_5.dto;

import java.util.List;

public class MonthAheadForecastResponse {
    private String assetType;
    private List<DailyForecastDTO> dailyForecasts;

    public MonthAheadForecastResponse() {}

    public MonthAheadForecastResponse(String assetType, List<DailyForecastDTO> dailyForecasts) {
        this.assetType = assetType;
        this.dailyForecasts = dailyForecasts;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public List<DailyForecastDTO> getDailyForecasts() {
        return dailyForecasts;
    }

    public void setDailyForecasts(List<DailyForecastDTO> dailyForecasts) {
        this.dailyForecasts = dailyForecasts;
    }
}