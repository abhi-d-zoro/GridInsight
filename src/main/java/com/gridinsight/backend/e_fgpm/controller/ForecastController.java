package com.gridinsight.backend.e_fgpm.controller;

import com.gridinsight.backend.e_fgpm.dto.*;
import com.gridinsight.backend.e_fgpm.repository.ForecastJobRepository;
import com.gridinsight.backend.e_fgpm.service.ForecastService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
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


    // -------------------------------------------------------------
    // ✅ Month-Ahead Forecast
    // -------------------------------------------------------------
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping("/month-ahead")
    public ResponseEntity<MonthAheadForecastResponse> getMonthAheadForecast(
            @RequestParam String assetType) {

        return ResponseEntity.ok(forecastService.generateMonthAheadForecast(assetType));
    }


    // -------------------------------------------------------------
    // ✅ Run Forecast Job
    // -------------------------------------------------------------
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @PostMapping("/run")
    public ResponseEntity<?> runForecast(@RequestBody ForecastRequest request) {

        if (request.getZoneId() == null) {
            return ResponseEntity.badRequest().body("zoneId is required");
        }
        if (request.getTargetDate() == null) {
            return ResponseEntity.badRequest().body("targetDate is required");
        }

        ForecastJobDTO pendingJob =
                forecastService.initiateForecast(request.getZoneId(), request.getTargetDate());

        forecastService.executeForecastAsync(pendingJob.getId());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(pendingJob);
    }


    // -------------------------------------------------------------
    // ✅ Get Job Status
    // -------------------------------------------------------------
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping("/job/{id}")
    public ResponseEntity<?> getJobStatus(@PathVariable Long id) {

        return repository.findById(id)
                .map(forecastService::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // -------------------------------------------------------------
    // ✅ Forecast Accuracy
    // -------------------------------------------------------------
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping("/accuracy")
    public ResponseEntity<?> getForecastAccuracy(
            @RequestParam String zoneId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            AccuracyResponse response =
                    forecastService.calculateAccuracy(zoneId, date.atStartOfDay());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    // -------------------------------------------------------------
    // ✅ CSV Export
    // -------------------------------------------------------------
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping(value = "/accuracy/export", produces = "text/csv")
    public ResponseEntity<?> exportAccuracyCsv(
            @RequestParam String zoneId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            AccuracyResponse resp =
                    forecastService.calculateAccuracy(zoneId, date.atStartOfDay());

            StringBuilder csv = new StringBuilder();

            csv.append("Zone ID,Date,Overall MAPE (%)\n");
            csv.append(zoneId).append(",")
                    .append(date).append(",")
                    .append(resp.getOverallMape()).append("\n\n");

            csv.append("Hour,Actual Load (MW),Forecast Load (MW),Error (%)\n");

            for (HourlyComparison h : resp.getHourlyData()) {
                csv.append(h.getHour()).append(",")
                        .append(h.getActualLoad()).append(",")
                        .append(h.getForecastLoad()).append(",")
                        .append(h.getErrorPercentage()).append("\n");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition",
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