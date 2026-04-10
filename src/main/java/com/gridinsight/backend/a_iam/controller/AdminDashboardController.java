package com.gridinsight.backend.a_iam.controller;

import com.gridinsight.backend.a_iam.dto.AdminKpiResponse;
import com.gridinsight.backend.a_iam.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/kpis")
    public ResponseEntity<AdminKpiResponse> getKpis() {
        return ResponseEntity.ok(adminDashboardService.getKpis());
    }
}