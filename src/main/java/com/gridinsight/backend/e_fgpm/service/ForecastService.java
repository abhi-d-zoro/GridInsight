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
import com.gridinsight.backend.e_fgpm.entity.MonthForecastRecord;
import com.gridinsight.backend.e_fgpm.repository.MonthForecastRepository;


import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ForecastService {

    private static final Logger log = LoggerFactory.getLogger(ForecastService.class);
    private final ForecastJobRepository repository;
    private final LoadRecordRepository loadDataRepository;
    private final MonthForecastRepository monthForecastRepository;

    // 🔹 Day-Ahead Forecast
    public DayAheadForecastResponse generateDayAheadForecast(String zoneId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusSeconds(1);

        Instant historyStart = start.minusDays(14).toInstant(ZoneOffset.UTC);
        Instant historyEnd = start.toInstant(ZoneOffset.UTC);

        List<LoadRecord> history = loadDataRepository
                .findByZoneIdAndTimestampBetweenOrderByTimestamp(zoneId, historyStart, historyEnd);

        List<Double> forecast = history.isEmpty()
                ? generateMock24HourData()
                : generateForecastFromHistory(history);

        Instant startInstant = start.toInstant(ZoneOffset.UTC);
        Instant endInstant = end.toInstant(ZoneOffset.UTC);

        List<LoadRecord> actualRecords = loadDataRepository
                .findByZoneIdAndTimestampBetweenOrderByTimestamp(zoneId, startInstant, endInstant);

        double[] actual = new double[24];
        for (LoadRecord l : actualRecords) {
            int h = l.getTimestamp().atZone(ZoneOffset.UTC).getHour();
            actual[h] = l.getDemandMW();
        }

        List<HourlyForecastDTO> hourlyData = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            double fc = forecast.size() > h ? forecast.get(h) : 0.0;
            double act = actual[h];
            hourlyData.add(new HourlyForecastDTO(h, round(fc), round(act)));
        }

        return new DayAheadForecastResponse(zoneId, date, hourlyData);
    }

    // 🔹 Month-Ahead Forecast
    // 🔹 Month-Ahead Forecast (database-driven)
    public MonthAheadForecastResponse generateMonthAheadForecast(String assetType) {
        List<MonthForecastRecord> records = monthForecastRepository
                .findByAssetTypeOrderByForecastDateAsc(assetType);

        List<DailyForecastDTO> daily = records.stream()
                .map(r -> new DailyForecastDTO(
                        r.getForecastDate(),
                        round(r.getForecastValue()),
                        round(r.getLowerBound()),
                        round(r.getUpperBound())))
                .toList();

        return new MonthAheadForecastResponse(assetType, daily);
    }


    @Transactional
    public ForecastJobDTO initiateForecast(String zoneId, LocalDate targetDate) {
        ForecastJob job = new ForecastJob();
        job.setZoneId(zoneId);
        job.setTargetDate(targetDate);
        job.setModelVersion("v1.0");
        job.setStatus("PENDING");
        job = repository.save(job);
        return toDTO(job);
    }

    @Async("forecastExecutor")
    @Transactional
    public void executeForecastAsync(Long jobId) {
        ForecastJob job = repository.findById(jobId).orElse(null);
        if (job == null) return;

        try {
            Thread.sleep(3000);
            LocalDateTime target = job.getTargetDate().atStartOfDay();
            LocalDateTime historyStart = target.minusDays(14);

            Instant start = historyStart.toInstant(ZoneOffset.UTC);
            Instant end = target.toInstant(ZoneOffset.UTC);

            List<LoadRecord> history =
                    loadDataRepository.findByZoneIdAndTimestampBetweenOrderByTimestamp(
                            job.getZoneId(), start, end);

            List<Double> forecast =
                    history.isEmpty() ? generateMock24HourData() : generateForecastFromHistory(history);

            job.setHourlyForecast(forecast);
            job.setStatus("COMPLETED");
        } catch (Exception e) {
            job.setStatus("FAILED");
        }
        repository.save(job);
    }

    // 🔹 Accuracy Calculation
    public AccuracyResponse calculateAccuracy(String zoneId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusSeconds(1);

        Optional<ForecastJob> jobOpt =
                repository.findFirstByZoneIdAndTargetDateAndStatusOrderByCreatedAtDesc(
                        zoneId, date, "COMPLETED");

        if (jobOpt.isEmpty()) {
            // Return safe empty response instead of throwing
            return new AccuracyResponse(zoneId, date.toString(), 0.0, Collections.emptyList());
        }

        ForecastJob job = jobOpt.get();
        List<Double> forecast = job.getHourlyForecast();

        Instant startInstant = start.toInstant(ZoneOffset.UTC);
        Instant endInstant = end.toInstant(ZoneOffset.UTC);

        List<LoadRecord> actualRecords =
                loadDataRepository.findByZoneIdAndTimestampBetweenOrderByTimestamp(
                        zoneId, startInstant, endInstant);

        double[] actual = new double[24];
        for (LoadRecord l : actualRecords) {
            int h = l.getTimestamp().atZone(ZoneOffset.UTC).getHour();
            actual[h] = l.getDemandMW();
        }

        List<HourlyComparison> hourly = new ArrayList<>();
        double totalErr = 0;
        int count = 0;

        for (int h = 0; h < 24; h++) {
            double act = actual[h];
            double fc = forecast.size() > h ? forecast.get(h) : 0.0;
            double err = 0;

            if (act > 0) {
                err = Math.abs((act - fc) / act) * 100;
                totalErr += err;
                count++;
            }

            hourly.add(new HourlyComparison(h, round(act), round(fc), round(err)));
        }

        double mape = count > 0 ? round(totalErr / count) : 0;
        return new AccuracyResponse(zoneId, date.toString(), mape, hourly);
    }

    // 🔹 Helpers
    private List<Double> generateForecastFromHistory(List<LoadRecord> history) {
        double[] sum = new double[24];
        int[] cnt = new int[24];
        for (LoadRecord l : history) {
            int h = l.getTimestamp().atZone(ZoneOffset.UTC).getHour();
            sum[h] += l.getDemandMW();
            cnt[h]++;
        }
        List<Double> out = new ArrayList<>();
        Random r = new Random();
        for (int h = 0; h < 24; h++) {
            if (cnt[h] > 0) {
                double avg = sum[h] / cnt[h];
                double noise = avg * 0.05 * (r.nextDouble() - 0.5);
                out.add(round(avg + noise));
            } else {
                out.add(round(100 + 400 * r.nextDouble()));
            }
        }
        return out;
    }

    private List<Double> generateMock24HourData() {
        Random r = new Random();
        List<Double> mock = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            mock.add(round(100 + 400 * r.nextDouble()));
        }
        return mock;
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public ForecastJobDTO toDTO(ForecastJob job) {
        return ForecastJobDTO.builder()
                .id(job.getId())
                .zoneId(Long.valueOf(job.getZoneId()))
                .modelVersion(job.getModelVersion())
                .status(job.getStatus())
                .targetDate(job.getTargetDate())
                .createdAt(job.getCreatedAt())
                .hourlyForecast(job.getHourlyForecast())
                .build();
    }
}
