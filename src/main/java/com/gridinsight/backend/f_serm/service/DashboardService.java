package com.gridinsight.backend.f_serm.service;

import com.gridinsight.backend.f_serm.dto.DashboardSummary;
import com.gridinsight.backend.f_serm.dto.SustainabilityMetricDTO;
import com.gridinsight.backend.f_serm.entity.SustainabilityMetric;
import com.gridinsight.backend.f_serm.repository.SustainabilityMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SustainabilityMetricRepository repository;

    public DashboardSummary getDashboardSummary(String period) {
        List<SustainabilityMetric> metrics = repository.findAll();

        double avgRenewableShare = metrics.stream()
                .mapToDouble(SustainabilityMetric::getRenewableSharePct)
                .average()
                .orElse(0.0);

        double totalEmissionsAvoided = metrics.stream()
                .mapToDouble(SustainabilityMetric::getEmissionsAvoidedTons)
                .sum();

        // ✅ Convert entity list → DTO list
        List<SustainabilityMetricDTO> metricDTOs = metrics.stream()
                .map(this::toDTO)
                .toList();

        return new DashboardSummary(avgRenewableShare, totalEmissionsAvoided, metricDTOs);
    }

    // ✅ Mapping method
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