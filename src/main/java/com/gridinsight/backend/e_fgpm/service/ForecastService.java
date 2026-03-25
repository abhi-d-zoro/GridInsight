package com.gridinsight.backend.e_fgpm.service;

import com.gridinsight.backend.d_lmdam.entity.LoadRecord;
import com.gridinsight.backend.d_lmdam.repository.LoadRecordRepository;
import com.gridinsight.backend.e_fgpm.dto.*;
import com.gridinsight.backend.e_fgpm.entity.ForecastJob;
import com.gridinsight.backend.e_fgpm.repository.ForecastJobRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ForecastService {



    private static final Logger log = LoggerFactory.getLogger(ForecastService.class);

    private final ForecastJobRepository repository;
    private final LoadRecordRepository loadDataRepository;

    // ===============================================================
    // US016: Month-Ahead Generation Forecast
    // ===============================================================
    public MonthAheadForecastResponse generateMonthAheadForecast(String assetType) {
        log.info("Generating month-ahead forecast for assetType={}", assetType);

        List<DailyForecastDTO> dailyForecasts = new ArrayList<>();
        LocalDate startDate = LocalDate.now();
        Random random = new Random();

        double baseMW = assetType.equalsIgnoreCase("SOLAR") ? 50.0 : 120.0;

        for (int i = 0; i < 30; i++) {
            LocalDate targetDate = startDate.plusDays(i);

            double dailyValue = baseMW + (random.nextDouble() * 10 - 5);
            double lowerBound = dailyValue * 0.95;
            double upperBound = dailyValue * 1.05;

            dailyValue = Math.round(dailyValue * 100.0) / 100.0;
            lowerBound = Math.round(lowerBound * 100.0) / 100.0;
            upperBound = Math.round(upperBound * 100.0) / 100.0;

            dailyForecasts.add(new DailyForecastDTO(targetDate, dailyValue, lowerBound, upperBound));
        }

        return new MonthAheadForecastResponse(assetType, dailyForecasts);
    }

    // ===============================================================
    // CREATE FORECAST JOB — return DTO
    // ===============================================================
    @Transactional
    public ForecastJobDTO initiateForecast(String zoneId, LocalDateTime targetDate) {

        ForecastJob job = new ForecastJob();
        job.setZoneId(zoneId);
        job.setTargetDate(targetDate);
        job.setModelVersion("v1.0-linear-regression");
        job.setStatus("PENDING");

        job = repository.save(job);
        repository.flush();

        log.info("Forecast job {} created with status PENDING for zone={}, targetDate={}",
                job.getId(), zoneId, targetDate);

        return toDTO(job);
    }

    // ===============================================================
    // ASYNC FORECAST EXECUTION (No change)
    // ===============================================================
    @Async("forecastExecutor")
    @Transactional
    public void executeForecastAsync(Long jobId) {
        log.info("Async forecast execution started for jobId={}", jobId);

        ForecastJob job = repository.findById(jobId).orElse(null);
        if (job == null) {
            log.error("Forecast job {} not found — cannot execute.", jobId);
            return;
        }

        try {
            Thread.sleep(5000);

            LocalDateTime to = job.getTargetDate();
            LocalDateTime from = to.minusDays(14);

            Instant fromInstant = from.toInstant(ZoneOffset.UTC);
            Instant toInstant = to.toInstant(ZoneOffset.UTC);
            Long parsedZoneId = Long.valueOf(job.getZoneId());

            List<LoadRecord> historicalData =
                    loadDataRepository.findByZoneIdAndTimestampBetweenOrderByTimestamp(
                            parsedZoneId, fromInstant, toInstant
                    );

            List<Double> forecast;

            if (historicalData.isEmpty()) {
                log.warn("No historical load data found for zone={} — using mock data.", job.getZoneId());
                forecast = generateMock24HourData();
            } else {
                forecast = generateForecastFromHistory(historicalData);
            }

            job.setHourlyForecast(forecast);
            job.setStatus("COMPLETED");

            log.info("Forecast job {} COMPLETED with {} hourly values", jobId, forecast.size());

        } catch (Exception e) {
            log.error("Forecast job {} FAILED", jobId, e);
            job.setStatus("FAILED");
        }

        repository.save(job);
    }

    private List<Double> generateForecastFromHistory(List<LoadRecord> history) {
        double[] sumByHour = new double[24];
        int[] countByHour = new int[24];

        for (LoadRecord ld : history) {
            int hour = ld.getTimestamp().atZone(ZoneOffset.UTC).getHour();
            sumByHour[hour] += ld.getDemandMW();
            countByHour[hour]++;
        }

        List<Double> forecast = new ArrayList<>(24);
        Random jitter = new Random();

        for (int h = 0; h < 24; h++) {
            if (countByHour[h] > 0) {
                double avg = sumByHour[h] / countByHour[h];
                double noise = avg * 0.05 * (jitter.nextDouble() - 0.5);
                forecast.add(Math.round((avg + noise) * 100.0) / 100.0);
            } else {
                forecast.add(100.0 + (400.0 * jitter.nextDouble()));
            }
        }
        return forecast;
    }

    private List<Double> generateMock24HourData() {
        List<Double> forecast = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 24; i++) {
            forecast.add(100.0 + (400.0 * random.nextDouble()));
        }
        return forecast;
    }

    // ===============================================================
    // FORECAST ACCURACY — already returns DTO
    // ===============================================================
    public AccuracyResponse calculateAccuracy(String zoneId, LocalDateTime targetDate) {
        Optional<ForecastJob> jobOpt =
                repository.findFirstByZoneIdAndTargetDateAndStatusOrderByCreatedAtDesc(zoneId, targetDate, "COMPLETED");

        if (jobOpt.isEmpty()) {
            throw new RuntimeException("No completed forecast found for zone " + zoneId);
        }

        ForecastJob job = jobOpt.get();
        List<Double> forecastValues = job.getHourlyForecast();

        LocalDateTime endOfDay = targetDate.plusDays(1).minusSeconds(1);

        Long parsedZoneId = Long.valueOf(zoneId);
        Instant startInstant = targetDate.toInstant(ZoneOffset.UTC);
        Instant endInstant = endOfDay.toInstant(ZoneOffset.UTC);

        List<LoadRecord> actuals =
                loadDataRepository.findByZoneIdAndTimestampBetweenOrderByTimestamp(parsedZoneId, startInstant, endInstant);

        double[] actualLoadByHour = new double[24];

        for (LoadRecord ld : actuals) {
            int hour = ld.getTimestamp().atZone(ZoneOffset.UTC).getHour();
            actualLoadByHour[hour] = ld.getDemandMW();
        }

        List<HourlyComparison> comparisons = new ArrayList<>();
        double totalError = 0.0;
        int valid = 0;

        for (int i = 0; i < 24; i++) {
            double forecast = forecastValues.get(i);
            double actual = actualLoadByHour[i];

            double err = 0.0;
            if (actual > 0) {
                err = Math.abs((actual - forecast) / actual) * 100.0;
                totalError += err;
                valid++;
            }

            comparisons.add(new HourlyComparison(i,
                    Math.round(actual * 100.0) / 100.0,
                    forecast,
                    Math.round(err * 100.0) / 100.0));
        }

        double mape = valid > 0 ? (totalError / valid) : 0.0;

        return new AccuracyResponse(zoneId, targetDate.toLocalDate().toString(),
                Math.round(mape * 100.0) / 100.0, comparisons);
    }

    // ===============================================================
    // MAPPER — ForecastJob → ForecastJobDTO
    // ===============================================================
    public ForecastJobDTO toDTO(ForecastJob job) {
        return ForecastJobDTO.builder()
                .id(job.getId())
                .zoneId(job.getZoneId())
                .modelVersion(job.getModelVersion())
                .status(job.getStatus())
                .targetDate(job.getTargetDate())
                .createdAt(job.getCreatedAt())
                .hourlyForecast(job.getHourlyForecast())
                .build();
    }
}