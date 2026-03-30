package com.gridinsight.backend.g_atmm.controller;

import com.gridinsight.backend.g_atmm.dto.ThresholdRuleDTO;
import com.gridinsight.backend.g_atmm.dto.ThresholdRuleRequestDTO;
import com.gridinsight.backend.g_atmm.service.ThresholdRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/threshold-rules")
@RequiredArgsConstructor
public class ThresholdRuleController {

    private final ThresholdRuleService service;

    // ✅ Only ADMIN can create threshold rules
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ThresholdRuleDTO create(@Valid @RequestBody ThresholdRuleRequestDTO request) {
        return service.create(request);
    }

    // ✅ Only ADMIN can update threshold rules
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ThresholdRuleDTO update(
            @PathVariable Long id,
            @Valid @RequestBody ThresholdRuleRequestDTO request
    ) {
        return service.update(id, request);
    }

    // ✅ Only ADMIN can delete threshold rules
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // ✅ ADMIN + GRID_ANALYST can fetch a single threshold rule
    @PreAuthorize("hasAnyRole('ADMIN','GRID_ANALYST')")
    @GetMapping("/{id}")
    public ThresholdRuleDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // ✅ ADMIN + GRID_ANALYST can fetch all threshold rules
    @PreAuthorize("hasAnyRole('ADMIN','GRID_ANALYST')")
    @GetMapping
    public List<ThresholdRuleDTO> getAll() {
        return service.getAll();
    }
}