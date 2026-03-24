package com.gridinsight.backend.e_fgpm.controller;

import com.gridinsight.backend.e_fgpm.dto.AccuracyResponse;
import com.gridinsight.backend.e_fgpm.dto.ForecastRequest;
import com.gridinsight.backend.e_fgpm.dto.HourlyComparison;
import com.gridinsight.backend.e_fgpm.dto.MonthAheadForecastResponse;
import com.gridinsight.backend.e_fgpm.entity.ForecastJob;
import com.gridinsight.backend.e_fgpm.service.ForecastService;
import com.gridinsight.backend.e_fgpm.repository.ForecastJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/forecast")
public class ForecastController {

    @Autowired
    private ForecastService forecastService;

    @Autowired
    private ForecastJobRepository repository;

    /**
     * US016: GET /api/v1/forecast/month-ahead
     *
     * Returns a 30-day generation forecast aggregated by asset type.
     */
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping("/month-ahead")
    public ResponseEntity<MonthAheadForecastResponse> getMonthAheadForecast(
            @RequestParam(name = "assetType") String assetType) {

        MonthAheadForecastResponse response = forecastService.generateMonthAheadForecast(assetType);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/forecast/run
     *
     * Triggers a day-ahead load forecast asynchronously.
     * Returns the job immediately with status = PENDING (HTTP 202 Accepted).
     * The actual computation runs in a background thread.
     * The frontend should poll GET /job/{id} to track progress.
     */
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @PostMapping("/run")
    public ResponseEntity<?> runForecast(@RequestBody ForecastRequest request) {
        if (request.getZoneId() == null || request.getZoneId().isBlank()) {
            return ResponseEntity.badRequest().body("zoneId is required");
        }
        if (request.getTargetDate() == null) {
            return ResponseEntity.badRequest()
                    .body("targetDate is required. Expected format: yyyy-MM-dd'T'HH:mm:ss (e.g. 2026-03-12T00:00:00)");
        }

        // Step 1: Save job as PENDING and return immediately
        ForecastJob pendingJob = forecastService.initiateForecast(
                request.getZoneId(), request.getTargetDate());

        // Step 2: Kick off the async background computation
        forecastService.executeForecastAsync(pendingJob.getId());

        // Return 202 Accepted with the PENDING job so the client can poll /job/{id}
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(pendingJob);
    }

    /**
     * GET /api/v1/forecast/job/{id}
     *
     * Returns the current state of a forecast job.
     * The frontend polls this to see when status changes from PENDING → COMPLETED / FAILED.
     */
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping("/job/{id}")
    public ResponseEntity<ForecastJob> getJobStatus(@PathVariable Long id) {
        Optional<ForecastJob> job = repository.findById(id);
        return job.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * US018: GET /api/v1/forecast/accuracy
     * Returns JSON data containing MAPE and hourly comparison for charts/tables.
     */
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping("/accuracy")
    public ResponseEntity<?> getForecastAccuracy(
            @RequestParam String zoneId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            LocalDateTime targetDate = date.atStartOfDay();
            AccuracyResponse response = forecastService.calculateAccuracy(zoneId, targetDate);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * US018: GET /api/v1/forecast/accuracy/export
     * Generates and returns a CSV file of the accuracy data.
     */
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping(value = "/accuracy/export", produces = "text/csv")
    public ResponseEntity<?> exportAccuracyCsv(
            @RequestParam String zoneId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            LocalDateTime targetDate = date.atStartOfDay();
            AccuracyResponse response = forecastService.calculateAccuracy(zoneId, targetDate);

            // Build CSV content
            StringBuilder csvBuilder = new StringBuilder();
            csvBuilder.append("Zone ID,Date,Overall MAPE (%)\n");
            csvBuilder.append(zoneId).append(",")
                    .append(date).append(",")
                    .append(response.getOverallMape()).append("\n\n");

            csvBuilder.append("Hour,Actual Load (MW),Forecast Load (MW),Error (%)\n");

            for (HourlyComparison hc : response.getHourlyData()) {
                csvBuilder.append(hc.getHour()).append(",")
                        .append(hc.getActualLoad()).append(",")
                        .append(hc.getForecastLoad()).append(",")
                        .append(hc.getErrorPercentage()).append("\n");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=forecast_accuracy_" + zoneId + "_" + date + ".csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csvBuilder.toString());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}