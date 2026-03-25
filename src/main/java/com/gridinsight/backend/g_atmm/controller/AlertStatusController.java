package com.gridinsight.backend.g_atmm.controller;

import com.gridinsight.backend.g_atmm.dto.AcknowledgeAlertDTO;
import com.gridinsight.backend.g_atmm.dto.AlertActivityDTO;
import com.gridinsight.backend.g_atmm.dto.AlertDTO;
import com.gridinsight.backend.g_atmm.dto.CloseAlertDTO;
import com.gridinsight.backend.g_atmm.service.AlertStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertStatusController {

    private final AlertStatusService statusService;

    // ✅ Only ADMIN can acknowledge alerts
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/acknowledge")
    public AlertDTO acknowledge(
            @PathVariable Long id,
            @Valid @RequestBody AcknowledgeAlertDTO request
    ) {
        return statusService.acknowledge(id, request);
    }

    // ✅ Only ADMIN can close alerts
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/close")
    public AlertDTO close(
            @PathVariable Long id,
            @Valid @RequestBody CloseAlertDTO request
    ) {
        return statusService.close(id, request);
    }

    // ✅ ADMIN + GRID_ANALYST can view alert activity
    @PreAuthorize("hasAnyRole('ADMIN','GRID_ANALYST')")
    @GetMapping("/{id}/activity")
    public List<AlertActivityDTO> getActivity(@PathVariable Long id) {
        return statusService.getActivity(id);
    }
}