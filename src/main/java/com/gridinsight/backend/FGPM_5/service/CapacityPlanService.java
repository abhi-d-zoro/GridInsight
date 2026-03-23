package com.gridinsight.backend.FGPM_5.service;

import com.gridinsight.backend.FGPM_5.dto.CapacityPlanRequest;
import com.gridinsight.backend.FGPM_5.entity.CapacityPlan;
import com.gridinsight.backend.FGPM_5.repository.CapacityPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CapacityPlanService {

    @Autowired
    private CapacityPlanRepository repository;

    public CapacityPlan createCapacityPlan(CapacityPlanRequest request) {
        CapacityPlan plan = new CapacityPlan();
        plan.setZoneId(request.getZoneId());
        plan.setHorizon(request.getHorizon());
        plan.setRecommendedCapacityMw(request.getRecommendedCapacityMw());
        plan.setNotes(request.getNotes());

        // Logic for Versioning
        int nextVersion = repository.findTopByZoneIdOrderByPlanVersionDesc(request.getZoneId())
                .map(existingPlan -> existingPlan.getPlanVersion() + 1)
                .orElse(1); // Start at version 1 if no previous plans exist

        plan.setPlanVersion(nextVersion);

        return repository.save(plan);
    }

    // Placeholder for PDF Export Acceptance Criteria
    public byte[] exportPlanToPdf(Long planId) {
        CapacityPlan plan = repository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        // In a real scenario, you'd use a library like iText or Apache PDFBox here.
        String dummyPdfContent = "PDF Report for Zone: " + plan.getZoneId() +
                " | Horizon: " + plan.getHorizon() +
                " | Capacity: " + plan.getRecommendedCapacityMw() + "MW";

        return dummyPdfContent.getBytes();
    }
}