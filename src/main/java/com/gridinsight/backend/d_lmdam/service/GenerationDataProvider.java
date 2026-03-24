package com.gridinsight.backend.d_lmdam.service;

import java.time.Instant;
import java.util.List;

/**
 * Supplies generated energy time-series for a zone within a window.
 * Implement this in your Renewable module and Spring will inject it into LoadMonitoringService.
 */
public interface GenerationDataProvider {

    /**
     * Immutable point on the time-series.
     */
    record GenerationPoint(Instant ts, double generatedMW) {}

    /**
     * @param zoneId the zone to query
     * @param from   inclusive (UTC)
     * @param to     inclusive (UTC)
     * @return a list of points ordered by timestamp (ascending).
     */
    List<GenerationPoint> fetchGenerationSeries(Long zoneId, Instant from, Instant to);
}