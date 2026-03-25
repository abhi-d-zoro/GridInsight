package com.gridinsight.backend.f_serm.service;

import com.gridinsight.backend.f_serm.dto.SustainabilityMetricDTO;
import com.gridinsight.backend.f_serm.entity.EnergyData;
import com.gridinsight.backend.f_serm.entity.SustainabilityMetric;
import com.gridinsight.backend.f_serm.repository.EnergyDataRepository;
import com.gridinsight.backend.f_serm.repository.SustainabilityMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SustainabilityMetricService {

    private final SustainabilityMetricRepository metricRepo;
    private final EnergyDataRepository energyRepo;

    public SustainabilityMetricDTO computeAndSaveMetric(String period) {

        EnergyData data = energyRepo.findByPeriod(period)
                .orElseThrow(() -> new RuntimeException("No energy data found for period " + period));

        double renewableSharePct = (data.getRenewableGeneration() / data.getTotalGeneration()) * 100.0;
        double emissionsAvoidedTons = data.getEmissionsAvoided();

        SustainabilityMetric metric = new SustainabilityMetric();
        metric.setPeriod(period);
        metric.setRenewableSharePct(renewableSharePct);
        metric.setEmissionsAvoidedTons(emissionsAvoidedTons);
        metric.setGeneratedDate(LocalDate.now());

        SustainabilityMetric saved = metricRepo.save(metric);
        return toDTO(saved);
    }

    public List<SustainabilityMetricDTO> getAllMetrics() {
        return metricRepo.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    private SustainabilityMetricDTO toDTO(SustainabilityMetric m) {
        return SustainabilityMetricDTO.builder()
                .metricId(m.getMetricId())
                .period(m.getPeriod())
                .renewableSharePct(m.getRenewableSharePct())
                .emissionsAvoidedTons(m.getEmissionsAvoidedTons())
                .generatedDate(m.getGeneratedDate())
                .build();
    }
}