package com.gridinsight.backend.z_common.observability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * US028 — Custom health indicator that verifies both DB connectivity
 * AND the presence of critical tables (one per module).
 * <p>
 * Registered as {@code dbSchema} so it contributes to the readiness
 * probe group. When the database is unreachable or the schema is
 * incomplete, {@code GET /actuator/health/readiness} returns 503.
 */
@Component("dbSchema")
@RequiredArgsConstructor
@Slf4j
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    /**
     * One critical table per module — acts as automated schema
     * migration validation without Flyway .sql files.
     */
    private static final List<String> CRITICAL_TABLES = List.of(
            "users",                 // a_iam
            "roles",                 // a_iam
            "grid_zones",            // b_gtmpm
            "assets",                // c_rgmm
            "load_records",          // d_lmdam
            "forecast_jobs",         // e_fgpm
            "sustainability_metric", // f_serm
            "alerts",                // g_atmm
            "audit_logs"             // z_common
    );

    @Override
    public Health health() {
        List<String> missingTables = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            for (String table : CRITICAL_TABLES) {
                try (ResultSet rs = metaData.getTables(
                        conn.getCatalog(), null, table, new String[]{"TABLE"})) {
                    if (!rs.next()) {
                        missingTables.add(table);
                    }
                }
            }

            if (!missingTables.isEmpty()) {
                log.warn("US028 readiness: missing tables {}", missingTables);
                return Health.down()
                        .withDetail("reason", "Missing critical tables")
                        .withDetail("missingTables", missingTables)
                        .withDetail("checkedTables", CRITICAL_TABLES.size())
                        .build();
            }

            return Health.up()
                    .withDetail("checkedTables", CRITICAL_TABLES.size())
                    .withDetail("database", conn.getCatalog())
                    .build();

        } catch (Exception ex) {
            log.error("US028 readiness: DB connectivity failed", ex);
            return Health.down()
                    .withDetail("reason", "Database unreachable")
                    .withException(ex)
                    .build();
        }
    }
}

