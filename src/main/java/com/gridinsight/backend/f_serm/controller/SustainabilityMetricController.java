package com.gridinsight.backend.f_serm.controller;

import com.gridinsight.backend.f_serm.dto.SustainabilityMetricDTO;
import com.gridinsight.backend.f_serm.service.SustainabilityMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class SustainabilityMetricController {

    private final SustainabilityMetricService service;

    // ✅ Only ADMIN or ESG_ANALYST can compute sustainability metrics
    @PreAuthorize("hasAnyRole('ADMIN','ESG_ANALYST')")
    @PostMapping("/compute")
    public SustainabilityMetricDTO computeMetric(@RequestParam String period) {
        return service.computeAndSaveMetric(period);
    }

    // ✅ Only ADMIN or ESG_ANALYST can view sustainability metrics
    @PreAuthorize("hasAnyRole('ADMIN','ESG_ANALYST')")
    @GetMapping
    public List<SustainabilityMetricDTO> getMetrics() {
        return service.getAllMetrics();
    }
}