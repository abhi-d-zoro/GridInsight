package com.gridinsight.backend.SERM_6.service;

import com.gridinsight.backend.SERM_6.dto.DashboardSummary;
import com.gridinsight.backend.SERM_6.entity.SustainabilityMetric;
import com.gridinsight.backend.SERM_6.repository.SustainabilityMetricRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final SustainabilityMetricRepository repository;

    public DashboardService(SustainabilityMetricRepository repository) {
        this.repository = repository;
    }

    public DashboardSummary getDashboardSummary(String period) {
        List<SustainabilityMetric> metrics = repository.findAll();

        double avgRenewableShare = metrics.stream()
                .mapToDouble(SustainabilityMetric::getRenewableSharePct)
                .average()
                .orElse(0.0);

        double totalEmissionsAvoided = metrics.stream()
                .mapToDouble(SustainabilityMetric::getEmissionsAvoidedTons)
                .sum();

        return new DashboardSummary(avgRenewableShare, totalEmissionsAvoided, metrics);
    }
}
