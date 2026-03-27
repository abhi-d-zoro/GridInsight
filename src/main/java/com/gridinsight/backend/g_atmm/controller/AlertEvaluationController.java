package com.gridinsight.backend.g_atmm.controller;

import com.gridinsight.backend.g_atmm.dto.AlertDTO;
import com.gridinsight.backend.g_atmm.dto.CheckValueRequestDTO;
import com.gridinsight.backend.g_atmm.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class AlertEvaluationController {

    private final AlertService alertService;

    // ✅ Only ADMIN or GRID_ANALYST can evaluate threshold rules
    @PreAuthorize("hasAnyRole('ADMIN','GRID_ANALYST')")
    @PostMapping("/check-value")
    public List<AlertDTO> evaluate(@Valid @RequestBody CheckValueRequestDTO request) {
        return alertService.evaluate(request);
    }

    // ✅ Only ADMIN or GRID_ANALYST can view the list of alerts
    @PreAuthorize("hasAnyRole('ADMIN','GRID_ANALYST')")
    @GetMapping("/alerts")
    public List<AlertDTO> getAll() {
        return alertService.getAll();
    }

    // ✅ Only ADMIN or GRID_ANALYST can view a single alert
    @PreAuthorize("hasAnyRole('ADMIN','GRID_ANALYST')")
    @GetMapping("/alerts/{id}")
    public AlertDTO getById(@PathVariable Long id) {
        return alertService.getById(id);
    }
}