package com.gridinsight.backend.c_rgmm.service;

import com.gridinsight.backend.c_rgmm.dto.TrendPointDto;
import com.gridinsight.backend.c_rgmm.dto.TrendResponseDto;
import com.gridinsight.backend.c_rgmm.entity.GenerationRecord;
import com.gridinsight.backend.c_rgmm.repository.GenerationRecordRepository;
import lombok.RequiredArgsConstructor;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenerationTrendService {

    private final GenerationRecordRepository repository;

    public TrendResponseDto getTrends(String assetId, LocalDateTime start, LocalDateTime end) {
        List<GenerationRecord> records = repository.findAll().stream()
                .filter(r -> r.getAssetId().equals(assetId)
                        && !r.getTimestamp().isBefore(start)
                        && !r.getTimestamp().isAfter(end))
                .collect(Collectors.toList());

        List<TrendPointDto> points = records.stream()
                .map(r -> new TrendPointDto(r.getTimestamp(), r.getGeneratedEnergyMWh(), r.getAvailabilityPct()))
                .collect(Collectors.toList());

        return new TrendResponseDto(assetId, points);
    }

    public byte[] exportCsv(TrendResponseDto dto) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);
        writer.println("Timestamp,GeneratedEnergyMWh,AvailabilityPct");
        dto.getPoints().forEach(p ->
                writer.printf("%s,%.2f,%.2f%n", p.getTimestamp(), p.getEnergy(), p.getAvailability()));
        writer.flush();
        return out.toByteArray();
    }

    public byte[] exportPng(TrendResponseDto dto) {
        try {
            XYChart chart = new XYChartBuilder()
                    .width(800).height(600)
                    .title("Generation Trend - " + dto.getAssetId())
                    .xAxisTitle("Timestamp")
                    .yAxisTitle("GeneratedEnergyMWh")
                    .build();

            // Energy series
            List<String> timestamps = dto.getPoints().stream()
                    .map(p -> p.getTimestamp().toString())
                    .collect(Collectors.toList());
            List<Double> energies = dto.getPoints().stream()
                    .map(TrendPointDto::getEnergy)
                    .collect(Collectors.toList());
            chart.addSeries("Energy", timestamps, energies);

            // Availability overlay
            List<Double> availability = dto.getPoints().stream()
                    .map(TrendPointDto::getAvailability)
                    .collect(Collectors.toList());
            chart.addSeries("Availability", timestamps, availability);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BitmapEncoder.saveBitmap(chart, out, BitmapEncoder.BitmapFormat.PNG);
            return out.toByteArray();

        } catch (Exception e) {
            // Log the error and return empty image
            e.printStackTrace();
            return new byte[0];
        }
    }
}
