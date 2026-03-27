package com.gridinsight.backend.f_serm.controller;

import com.gridinsight.backend.f_serm.dto.DashboardSummary;
import com.gridinsight.backend.f_serm.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    // ✅ Only ADMIN or ESG_ANALYST can view the ESG dashboard
    @PreAuthorize("hasAnyRole('ADMIN','ESG')")
    @GetMapping
    public DashboardSummary getDashboard(@RequestParam(required = false) String period) {
        return service.getDashboardSummary(period);
    }
}