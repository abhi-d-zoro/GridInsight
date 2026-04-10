package com.gridinsight.backend.z_common.observability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * US028 — Runs once at startup after Hibernate has applied {@code ddl-auto=update}.
 * Validates that all expected tables exist and logs the schema state.
 * <p>
 * This replaces the need for Flyway {@code .sql} migration files:
 * Hibernate creates/updates the schema automatically, and this bean
 * confirms the result on every deployment.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaValidator {

    private final DataSource dataSource;

    /** Every table the application expects — one per entity. */
    private static final List<String> EXPECTED_TABLES = List.of(
            // a_iam
            "users", "roles", "user_roles",
            "refresh_tokens", "password_reset_tokens", "login_audit",
            // b_gtmpm
            "grid_zones", "measurement_points",
            // c_rgmm
            "assets", "generation_records",
            // d_lmdam
            "load_records", "peak_events",
            // e_fgpm
            "forecast_jobs", "forecast_hourly_results", "capacity_plans",
            // f_serm
            "sustainability_metric", "esgreport", "energy_data",
            // g_atmm
            "threshold_rules", "alerts", "alert_activity",
            // z_common
            "audit_logs"
    );

    @EventListener(ApplicationReadyEvent.class)
    public void validateSchemaOnStartup() {
        log.info("US028: Running post-startup schema validation...");

        List<String> present = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();

            for (String table : EXPECTED_TABLES) {
                try (ResultSet rs = metaData.getTables(catalog, null, table, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        present.add(table);
                    } else {
                        missing.add(table);
                    }
                }
            }

            log.info("US028: Schema validation complete - {}/{} tables present",
                    present.size(), EXPECTED_TABLES.size());

            if (!missing.isEmpty()) {
                log.warn("US028: Missing tables (may appear after first entity access): {}", missing);
            } else {
                log.info("US028: All expected tables verified. Deployment is safe.");
            }

        } catch (Exception ex) {
            log.error("US028: Schema validation FAILED - database unreachable", ex);
        }
    }
}

