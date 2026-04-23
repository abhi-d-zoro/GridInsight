package com.gridinsight.backend.e_fgpm.service;

import com.gridinsight.backend.e_fgpm.dto.CapacityPlanDTO;
import com.gridinsight.backend.e_fgpm.dto.CapacityPlanRequest;
import com.gridinsight.backend.e_fgpm.entity.CapacityPlan;
import com.gridinsight.backend.e_fgpm.repository.CapacityPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

@Service
@RequiredArgsConstructor
public class CapacityPlanService {

    private final CapacityPlanRepository repository;

    public CapacityPlanDTO createCapacityPlan(CapacityPlanRequest request) {
        CapacityPlan plan = new CapacityPlan();
        plan.setZoneId(request.getZoneId());
        plan.setHorizon(request.getHorizon());
        plan.setRecommendedCapacityMw(request.getRecommendedCapacityMw());
        plan.setNotes(request.getNotes());

        int nextVersion = repository.findTopByZoneIdOrderByPlanVersionDesc(request.getZoneId())
                .map(existingPlan -> existingPlan.getPlanVersion() + 1)
                .orElse(1);

        plan.setPlanVersion(nextVersion);

        CapacityPlan saved = repository.save(plan);
        return toDTO(saved);
    }

    public List<CapacityPlanDTO> getAllPlans() {
        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // Generate a real PDF
    public byte[] exportPlanToPdf(Long planId) {
        CapacityPlan plan = repository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Capacity Plan Report").setBold().setFontSize(16));
        document.add(new Paragraph("Zone: " + plan.getZoneId()));
        document.add(new Paragraph("Horizon: " + plan.getHorizon()));
        document.add(new Paragraph("Capacity: " + plan.getRecommendedCapacityMw() + " MW"));
        document.add(new Paragraph("Version: " + plan.getPlanVersion()));
        if (plan.getNotes() != null) {
            document.add(new Paragraph("Notes: " + plan.getNotes()));
        }

        document.close();
        return baos.toByteArray();
    }

    private CapacityPlanDTO toDTO(CapacityPlan plan) {
        CapacityPlanDTO dto = new CapacityPlanDTO();
        dto.setId(plan.getId());
        dto.setZoneId(plan.getZoneId());
        dto.setHorizon(plan.getHorizon());
        dto.setRecommendedCapacityMw(plan.getRecommendedCapacityMw());
        dto.setNotes(plan.getNotes());
        dto.setPlanVersion(plan.getPlanVersion());
        dto.setCreatedAt(plan.getCreatedAt());
        return dto;
    }
}
