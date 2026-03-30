package com.gridinsight.backend.z_common.observability;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * US026 — AOP aspect that collects business metrics transparently.
 * <p>
 * Pointcut expressions target existing service methods so that <b>no code
 * changes are required</b> in any module controller or service.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsAspect {

    private final ObservabilityMetrics metrics;

    /* ------------------------------------------------------------------ */
    /*  Ingest rate – LoadMonitoringService.createLoadRecord(..)          */
    /* ------------------------------------------------------------------ */
    @AfterReturning("execution(* com.gridinsight.backend.d_lmdam.service.LoadMonitoringService.createLoadRecord(..))")
    public void countIngest() {
        metrics.ingestCounter().increment();
        log.debug("Metric incremented: gridinsight.ingest.total");
    }

    /* ------------------------------------------------------------------ */
    /*  Alert rate – AlertService.evaluate(..)                            */
    /*  We count based on the size of the returned list (alerts fired).   */
    /* ------------------------------------------------------------------ */
    @AfterReturning(
            pointcut = "execution(* com.gridinsight.backend.g_atmm.service.AlertService.evaluate(..))",
            returning = "result")
    public void countAlerts(Object result) {
        if (result instanceof Collection<?> list) {
            metrics.alertCounter().increment(list.size());
            log.debug("Metric incremented: gridinsight.alerts.total by {}", list.size());
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Dashboard latency – DashboardService.getDashboardSummary(..)      */
    /* ------------------------------------------------------------------ */
    @Around("execution(* com.gridinsight.backend.f_serm.service.DashboardService.getDashboardSummary(..))")
    public Object timeDashboard(ProceedingJoinPoint pjp) throws Throwable {
        Timer.Sample sample = Timer.start();
        try {
            return pjp.proceed();
        } finally {
            sample.stop(metrics.dashboardTimer());
            log.debug("Metric recorded: gridinsight.dashboard.latency");
        }
    }
}

