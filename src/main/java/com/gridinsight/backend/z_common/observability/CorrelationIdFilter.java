package com.gridinsight.backend.z_common.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * US026 — Populates SLF4J MDC with {@code correlationId} (and later {@code userId})
 * so that every log line in the request automatically carries these fields.
 * <p>
 * Runs at {@link Ordered#HIGHEST_PRECEDENCE} so it executes before Spring Security
 * and before the JWT filter — the correlationId is therefore available even in
 * authentication-related log lines.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String MDC_CORRELATION_ID    = "correlationId";
    public static final String MDC_USER_ID           = "userId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1) Propagate or generate correlationId
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }
            MDC.put(MDC_CORRELATION_ID, correlationId);

            // Store on request attribute so AuditLogService.getOrCreateCorrelationId() stays compatible
            request.setAttribute("correlationId", correlationId);

            // Echo back for client-side tracing
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            filterChain.doFilter(request, response);

        } finally {
            // Clean up MDC to prevent thread-pool leaks
            MDC.remove(MDC_CORRELATION_ID);
            MDC.remove(MDC_USER_ID);
        }
    }
}

