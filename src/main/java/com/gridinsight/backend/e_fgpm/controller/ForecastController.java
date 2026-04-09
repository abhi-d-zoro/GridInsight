package com.gridinsight.backend.e_fgpm.controller;

import com.gridinsight.backend.e_fgpm.dto.*;
import com.gridinsight.backend.e_fgpm.entity.ForecastJob;
import com.gridinsight.backend.e_fgpm.repository.ForecastJobRepository;
import com.gridinsight.backend.e_fgpm.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/forecast")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;
    private final ForecastJobRepository repository;

    // 🔹 Jobs Endpoints
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping("/jobs")
    public List<ForecastJobDTO> getAllJobs() {
        return repository.findAll()
                .stream()
                .map(forecastService::toDTO)
                .toList();
    }

    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping("/jobs/by-zone")
    public List<ForecastJobDTO> getJobsByZone(@RequestParam String zoneId) {
        return repository.findByZoneId(zoneId)
                .stream()
                .map(forecastService::toDTO)
                .toList();
    }

    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping("/jobs/by-date")
    public List<ForecastJobDTO> getJobsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return repository.findByTargetDate(date)
                .stream()
                .map(forecastService::toDTO)
                .toList();
    }

    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping("/jobs/by-zone-and-date")
    public List<ForecastJobDTO> getJobsByZoneAndDate(
            @RequestParam String zoneId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return repository.findByZoneIdAndTargetDate(zoneId, date)
                .stream()
                .map(forecastService::toDTO)
                .toList();
    }

    // 🔹 NEW: Day-Ahead Forecast
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping("/day-ahead")
    public ResponseEntity<DayAheadForecastResponse> getDayAheadForecast(
            @RequestParam String zoneId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(forecastService.generateDayAheadForecast(zoneId, date));
    }

    // 🔹 Month-Ahead Forecast
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping("/month-ahead")
    public ResponseEntity<MonthAheadForecastResponse> getMonthAheadForecast(
            @RequestParam String assetType) {
        return ResponseEntity.ok(forecastService.generateMonthAheadForecast(assetType));
    }

    // 🔹 Run Forecast Job
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

    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping("/job/{id}")
    public ResponseEntity<?> getJobStatus(@PathVariable Long id) {
        Optional<ForecastJob> job = repository.findById(id);
        return job.map(forecastService::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔹 Update Job Status
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @PutMapping("/job/{id}/status")
    public ResponseEntity<?> updateJobStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<ForecastJob> jobOpt = repository.findById(id);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String normalized = status.toUpperCase();
        if (!List.of("PENDING", "COMPLETED", "FAILED").contains(normalized)) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        }

        ForecastJob job = jobOpt.get();
        job.setStatus(normalized);
        repository.save(job);

        return ResponseEntity.ok(forecastService.toDTO(job));
    }

    // 🔹 Accuracy
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping("/accuracy")
    public ResponseEntity<?> getForecastAccuracy(
            @RequestParam String zoneId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            AccuracyResponse response =
                    forecastService.calculateAccuracy(zoneId, date);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // 🔹 Accuracy Export CSV
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping(value = "/accuracy/export", produces = "text/csv")
    public ResponseEntity<?> exportAccuracyCsv(
            @RequestParam String zoneId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            AccuracyResponse resp =
                    forecastService.calculateAccuracy(zoneId, date);

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
