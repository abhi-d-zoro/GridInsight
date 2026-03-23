package com.gridinsight.backend.FGPM_5.exception;

public class ForecastJobNotFoundException extends RuntimeException {
    public ForecastJobNotFoundException(Long id) {
        super("Forecast job not found with id: " + id);
    }
}

