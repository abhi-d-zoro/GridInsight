package com.gridinsight.backend.SERM_6.controller;

import com.gridinsight.backend.SERM_6.dto.DashboardSummary;
import com.gridinsight.backend.SERM_6.service.DashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    /**
     * Get ESG dashboard summary for a given period.
     * Example: GET /api/dashboard?period=2026-01
     * Requires ADMIN role.
     */
    @GetMapping
    public DashboardSummary getDashboard(@RequestParam(required = false) String period) {
        return service.getDashboardSummary(period);
    }
}
