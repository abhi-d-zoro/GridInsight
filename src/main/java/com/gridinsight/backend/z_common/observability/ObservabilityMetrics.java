package com.gridinsight.backend.z_common.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * US026 — Central holder for all application-level Micrometer metrics.
 * <p>
 * Metrics are registered once at startup; the {@link MetricsAspect} (and any
 * future service) can inject this bean to record values.
 * <p>
 * Exposed via {@code /actuator/prometheus} and {@code /actuator/metrics}.
 */
@Component
public class ObservabilityMetrics {

    /** Incremented every time a load / generation record is ingested. */
    private final Counter ingestCounter;

    /** Incremented every time a threshold alert is fired. */
    private final Counter alertCounter;

    /** Records the time taken to build the ESG dashboard summary. */
    private final Timer dashboardTimer;

    public ObservabilityMetrics(MeterRegistry registry) {
        this.ingestCounter = Counter.builder("gridinsight.ingest.total")
                .description("Number of load/generation records ingested")
                .register(registry);

        this.alertCounter = Counter.builder("gridinsight.alerts.total")
                .description("Number of threshold alerts fired")
                .register(registry);

        this.dashboardTimer = Timer.builder("gridinsight.dashboard.latency")
                .description("Time to serve the dashboard summary")
                .register(registry);
    }

    public Counter ingestCounter()   { return ingestCounter; }
    public Counter alertCounter()    { return alertCounter; }
    public Timer   dashboardTimer()  { return dashboardTimer; }
}

