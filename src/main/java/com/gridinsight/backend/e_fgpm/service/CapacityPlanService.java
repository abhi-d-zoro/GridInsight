package com.gridinsight.backend.e_fgpm.service;

import com.gridinsight.backend.e_fgpm.dto.CapacityPlanDTO;
import com.gridinsight.backend.e_fgpm.dto.CapacityPlanRequest;
import com.gridinsight.backend.e_fgpm.entity.CapacityPlan;
import com.gridinsight.backend.e_fgpm.repository.CapacityPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CapacityPlanService {

    private final CapacityPlanRepository repository;

    // ✅ Return DTO instead of entity
    public CapacityPlanDTO createCapacityPlan(CapacityPlanRequest request) {

        CapacityPlan plan = new CapacityPlan();
        plan.setZoneId(request.getZoneId());
        plan.setHorizon(request.getHorizon());
        plan.setRecommendedCapacityMw(request.getRecommendedCapacityMw());
        plan.setNotes(request.getNotes());

        // ✅ Versioning logic unchanged
        int nextVersion = repository.findTopByZoneIdOrderByPlanVersionDesc(request.getZoneId())
                .map(existing -> existing.getPlanVersion() + 1)
                .orElse(1);
        plan.setPlanVersion(nextVersion);

        CapacityPlan saved = repository.save(plan);

        return toDTO(saved);
    }

    // ✅ PDF export remains unchanged — returns bytes
    public byte[] exportPlanToPdf(Long planId) {
        CapacityPlan plan = repository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        String dummyPdfContent = "PDF Report for Zone: " + plan.getZoneId() +
                " | Horizon: " + plan.getHorizon() +
                " | Capacity: " + plan.getRecommendedCapacityMw() + "MW";

        return dummyPdfContent.getBytes();
    }

    // ✅ ENTITY ➜ DTO MAPPER
    private CapacityPlanDTO toDTO(CapacityPlan plan) {
        return CapacityPlanDTO.builder()
                .id(plan.getId())
                .zoneId(plan.getZoneId())
                .horizon(plan.getHorizon())
                .recommendedCapacityMw(plan.getRecommendedCapacityMw())
                .notes(plan.getNotes())
                .planVersion(plan.getPlanVersion())
                .createdAt(plan.getCreatedAt())
                .build();
    }
}