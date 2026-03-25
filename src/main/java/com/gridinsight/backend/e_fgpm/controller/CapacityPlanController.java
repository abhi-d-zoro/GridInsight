package com.gridinsight.backend.e_fgpm.controller;

import com.gridinsight.backend.e_fgpm.dto.CapacityPlanDTO;
import com.gridinsight.backend.e_fgpm.dto.CapacityPlanRequest;
import com.gridinsight.backend.e_fgpm.service.CapacityPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/capacity-plans")
@RequiredArgsConstructor
public class CapacityPlanController {

    private final CapacityPlanService service;

    // ✅ Now returns DTO
    @PreAuthorize("hasAnyRole('PLANNER','ADMIN')")
    @PostMapping
    public ResponseEntity<CapacityPlanDTO> createPlan(@RequestBody CapacityPlanRequest request) {
        CapacityPlanDTO createdPlan = service.createCapacityPlan(request);
        return new ResponseEntity<>(createdPlan, HttpStatus.CREATED);
    }

    // ✅ PDF unchanged
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