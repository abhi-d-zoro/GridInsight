package com.gridinsight.backend.e_fgpm.controller;

import com.gridinsight.backend.e_fgpm.dto.CapacityPlanDTO;
import com.gridinsight.backend.e_fgpm.dto.CapacityPlanRequest;
import com.gridinsight.backend.e_fgpm.service.CapacityPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//changed by tabassum
@RestController
@RequestMapping("/api/v1/capacity-plans")
@RequiredArgsConstructor
public class CapacityPlanController {

    private final CapacityPlanService service;

    // ✅ NEW: Get all capacity plans
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping
    public List<CapacityPlanDTO> getAllPlans() {
        return service.getAllPlans();
    }

    // ✅ Create Plan
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @PostMapping
    public ResponseEntity<CapacityPlanDTO> createPlan(@RequestBody CapacityPlanRequest request) {
        CapacityPlanDTO createdPlan = service.createCapacityPlan(request);
        return new ResponseEntity<>(createdPlan, HttpStatus.CREATED);
    }

    // ✅ PDF Export
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @GetMapping("/{id}/export-pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        byte[] pdfBytes = service.exportPlanToPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "capacity-plan-" + id + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
