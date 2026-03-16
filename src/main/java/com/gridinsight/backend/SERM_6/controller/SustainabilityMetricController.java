package com.gridinsight.backend.SERM_6.controller;

import com.gridinsight.backend.SERM_6.entity.SustainabilityMetric;
import com.gridinsight.backend.SERM_6.service.SustainabilityMetricService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
public class SustainabilityMetricController {

    private final SustainabilityMetricService service;

    public SustainabilityMetricController(SustainabilityMetricService service) {
        this.service = service;
    }

    @PostMapping("/compute")
    public SustainabilityMetric computeMetric(@RequestParam String period) {
        return service.computeAndSaveMetric(period);
    }

    @GetMapping
    public List<SustainabilityMetric> getMetrics() {
        return service.getAllMetrics();
    }
}
