package com.gridinsight.backend.b_gtmpm.controller;

import com.gridinsight.backend.b_gtmpm.dto.MeasurementPointRequestDTO;
import com.gridinsight.backend.b_gtmpm.dto.MeasurementPointResponseDTO;
import com.gridinsight.backend.b_gtmpm.dto.PageResponse;
import com.gridinsight.backend.b_gtmpm.service.MeasurementPointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/measurement-points")
@RequiredArgsConstructor
public class MeasurementPointController {

    private final MeasurementPointService service;

    @PostMapping
    public MeasurementPointResponseDTO create(@Valid @RequestBody MeasurementPointRequestDTO request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public MeasurementPointResponseDTO update(@PathVariable Long id,
                                              @Valid @RequestBody MeasurementPointRequestDTO request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public MeasurementPointResponseDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    public PageResponse<MeasurementPointResponseDTO> getAll(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long zoneId) {
        return service.getAll(page, size, q, zoneId);
    }
}