package com.gridinsight.backend.FGPM_5.controller;

import com.gridinsight.backend.FGPM_5.dto.CapacityPlanRequest;
import com.gridinsight.backend.FGPM_5.entity.CapacityPlan;
import com.gridinsight.backend.FGPM_5.service.CapacityPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/capacity-plans")
public class CapacityPlanController {

    @Autowired
    private CapacityPlanService service;

    // Endpoint to create a new versioned plan
    @PostMapping
    public ResponseEntity<CapacityPlan> createPlan(@RequestBody CapacityPlanRequest request) {
        CapacityPlan createdPlan = service.createCapacityPlan(request);
        return new ResponseEntity<>(createdPlan, HttpStatus.CREATED);
    }

    // Endpoint for the PDF export placeholder
    @GetMapping("/{id}/export-pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        byte[] pdfBytes = service.exportPlanToPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "capacity-plan-" + id + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}