package com.gridinsight.backend.e_fgpm.controller;

import com.gridinsight.backend.e_fgpm.dto.*;
import com.gridinsight.backend.e_fgpm.repository.ForecastJobRepository;
import com.gridinsight.backend.e_fgpm.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/forecast")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;
    private final ForecastJobRepository repository;

    // ✅ DTO: Month-ahead forecast
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping("/month-ahead")
    public ResponseEntity<MonthAheadForecastResponse> getMonthAheadForecast(
            @RequestParam(name = "assetType") String assetType) {

        return ResponseEntity.ok(forecastService.generateMonthAheadForecast(assetType));
    }

    // ✅ DTO: Run forecast (returns ForecastJobDTO)
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @PostMapping("/run")
    public ResponseEntity<?> runForecast(@RequestBody ForecastRequest request) {

        if (request.getZoneId() == null || request.getZoneId().isBlank()) {
            return ResponseEntity.badRequest().body("zoneId is required");
        }
        if (request.getTargetDate() == null) {
            return ResponseEntity.badRequest().body(
                    "targetDate is required. Expected format: yyyy-MM-dd'T'HH:mm:ss"
            );
        }

        // ✅ Now returns ForecastJobDTO
        ForecastJobDTO pendingJob = forecastService.initiateForecast(
                request.getZoneId(),
                request.getTargetDate()
        );

        // Async job execution
        forecastService.executeForecastAsync(pendingJob.getId());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(pendingJob);
    }

    // ✅ DTO: Get forecast job status
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping("/job/{id}")
    public ResponseEntity<?> getJobStatus(@PathVariable Long id) {

        return repository.findById(id)
                .map(forecastService::toDTO)   // ✅ Convert entity → DTO
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ DTO: Forecast accuracy already returns DTO
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

    // ✅ CSV Export unchanged
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping(value = "/accuracy/export", produces = "text/csv")
    public ResponseEntity<?> exportAccuracyCsv(
            @RequestParam String zoneId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            LocalDateTime targetDate = date.atStartOfDay();
            AccuracyResponse response = forecastService.calculateAccuracy(zoneId, targetDate);

            StringBuilder csv = new StringBuilder();
            csv.append("Zone ID,Date,Overall MAPE (%)\n");
            csv.append(zoneId).append(",")
                    .append(date).append(",")
                    .append(response.getOverallMape()).append("\n\n");

            csv.append("Hour,Actual Load (MW),Forecast Load (MW),Error (%)\n");
            for (HourlyComparison hc : response.getHourlyData()) {
                csv.append(hc.getHour()).append(",")
                        .append(hc.getActualLoad()).append(",")
                        .append(hc.getForecastLoad()).append(",")
                        .append(hc.getErrorPercentage()).append("\n");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=forecast_accuracy_" + zoneId + "_" + date + ".csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csv.toString());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}