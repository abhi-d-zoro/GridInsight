package com.gridinsight.backend.f_serm.service;

import com.gridinsight.backend.f_serm.entity.SustainabilityMetric;
import com.gridinsight.backend.f_serm.entity.EnergyData;
import com.gridinsight.backend.f_serm.repository.SustainabilityMetricRepository;
import com.gridinsight.backend.f_serm.repository.EnergyDataRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SustainabilityMetricService {

    private final SustainabilityMetricRepository metricRepo;
    private final EnergyDataRepository energyRepo;

    public SustainabilityMetricService(SustainabilityMetricRepository metricRepo,
                                       EnergyDataRepository energyRepo) {
        this.metricRepo = metricRepo;
        this.energyRepo = energyRepo;
    }

    public SustainabilityMetric computeAndSaveMetric(String period) {
        // 🔹 Fetch actual energy data for the given period
        EnergyData data = energyRepo.findByPeriod(period)
                .orElseThrow(() -> new RuntimeException("No energy data found for period " + period));

        // 🔹 Compute metrics based on actual values
        double renewableSharePct = (data.getRenewableGeneration() / data.getTotalGeneration()) * 100.0;
        double emissionsAvoidedTons = data.getEmissionsAvoided();

        SustainabilityMetric metric = new SustainabilityMetric();
        metric.setPeriod(period);
        metric.setRenewableSharePct(renewableSharePct);
        metric.setEmissionsAvoidedTons(emissionsAvoidedTons);
        metric.setGeneratedDate(LocalDate.now());

        return metricRepo.save(metric);
    }

    public List<SustainabilityMetric> getAllMetrics() {
        return metricRepo.findAll();
    }
}
