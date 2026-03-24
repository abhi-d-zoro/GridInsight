package com.gridinsight.backend.e_fgpm.exception;

public class ForecastJobNotFoundException extends RuntimeException {
    public ForecastJobNotFoundException(Long id) {
        super("Forecast job not found with id: " + id);
    }
}

