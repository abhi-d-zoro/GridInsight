package com.gridinsight.backend.e_fgpm.service;

import com.gridinsight.backend.d_lmdam.entity.LoadRecord;
import com.gridinsight.backend.d_lmdam.repository.LoadRecordRepository;
import com.gridinsight.backend.e_fgpm.dto.*;
import com.gridinsight.backend.e_fgpm.entity.ForecastJob;
import com.gridinsight.backend.e_fgpm.entity.ForecastHourlyResult;
import com.gridinsight.backend.e_fgpm.entity.MonthForecastRecord;
import com.gridinsight.backend.e_fgpm.repository.ForecastJobRepository;
import com.gridinsight.backend.e_fgpm.repository.MonthForecastRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;


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

    private Double safeRound(Double value) {
        return value == null ? null : round(value);
    }

    public MonthAheadForecastResponse generateMonthAheadForecast(String assetType) {
        List<MonthForecastRecord> records =
                monthForecastRepository.findByAssetTypeOrderByForecastDateAsc(assetType);

        if (records == null || records.isEmpty()) {
            log.warn("No month-ahead forecasts found for assetType={}", assetType);
            return new MonthAheadForecastResponse(assetType, List.of());
        }

        List<DailyForecastDTO> daily = records.stream()
                .map(r -> new DailyForecastDTO(
                        r.getForecastDate(),
                        safeRound(r.getForecastValue()),
                        safeRound(r.getLowerBound()),
                        safeRound(r.getUpperBound())))
                .toList();

        return new MonthAheadForecastResponse(assetType, daily);
    }

    public MonthForecastRecord calculateBounds(MonthForecastRecord record) {
        double margin = 0.15;
        double forecast = record.getForecastValue();
        record.setLowerBound(forecast * (1 - margin));
        record.setUpperBound(forecast * (1 + margin));
        return record;
    }

    @Transactional
    public MonthForecastRecord saveMonthForecast(MonthForecastRecord record) {
        MonthForecastRecord withBounds = calculateBounds(record);
        return monthForecastRepository.save(withBounds);
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

            List<ForecastHourlyResult> hourlyResults = new ArrayList<>();
            for (int h = 0; h < forecast.size(); h++) {
                ForecastHourlyResult result = new ForecastHourlyResult();
                result.setHour(h);
                result.setForecastValue(forecast.get(h));
                result.setJob(job);
                hourlyResults.add(result);
            }

            job.setHourlyForecasts(hourlyResults);
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
            return new AccuracyResponse(zoneId, date.toString(), 0.0, Collections.emptyList());
        }

        ForecastJob job = jobOpt.get();
        List<ForecastHourlyResult> forecastResults = job.getHourlyForecasts();

        // ✅ Build lookup map once
        Map<Integer, Double> forecastMap = forecastResults.stream()
                .collect(Collectors.toMap(ForecastHourlyResult::getHour,
                        ForecastHourlyResult::getForecastValue));

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
            double fc = forecastMap.getOrDefault(h, 0.0); // ✅ use map lookup

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
        int[] count = new int[24];

        for (LoadRecord l : history) {
            int h = l.getTimestamp().atZone(ZoneOffset.UTC).getHour();
            sum[h] += l.getDemandMW();
            count[h]++;
        }

        List<Double> forecast = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            if (count[h] > 0) {
                double avg = sum[h] / count[h];
                forecast.add(round(avg));
            } else {
                // fallback: use last available day’s value for this hour
                forecast.add(round(getLastAvailableHour(history, h)));
            }
        }
        return forecast;
    }

    private double getLastAvailableHour(List<LoadRecord> history, int hour) {
        return history.stream()
                .filter(l -> l.getTimestamp().atZone(ZoneOffset.UTC).getHour() == hour)
                .mapToDouble(LoadRecord::getDemandMW)
                .reduce((first, second) -> second) // last value
                .orElse(0.0);
    }

    private List<Double> generateMock24HourData() {
        List<Double> baseline = new ArrayList<>();
        // Example: simple sinusoidal curve to mimic daily demand pattern
        for (int h = 0; h < 24; h++) {
            double base = 300 + 100 * Math.sin(Math.PI * h / 12); // peak midday
            baseline.add(round(base));
        }
        return baseline;
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public ForecastJobDTO toDTO(ForecastJob job) {
        List<HourlyForecastDTO> forecastValues = job.getHourlyForecasts().stream()
                .sorted(Comparator.comparingInt(ForecastHourlyResult::getHour))
                .map(r -> new HourlyForecastDTO(r.getHour(), r.getForecastValue(), 0.0)) // actual=0.0 here
                .toList();

        return ForecastJobDTO.builder()
                .id(job.getId())
                .zoneId(job.getZoneId())   // keep as String
                .modelVersion(job.getModelVersion())
                .status(job.getStatus())
                .targetDate(job.getTargetDate())
                .createdAt(job.getCreatedAt())
                .hourlyForecast(forecastValues)
                .build();
    }

}