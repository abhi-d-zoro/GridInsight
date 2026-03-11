package com.gridinsight.backend.IAM_1.controller;

import com.gridinsight.backend.IAM_1.dto.AuditLogResponse;
import com.gridinsight.backend.IAM_1.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * Admin-only: get audit logs filtered by user, action, resource, date range.
     * Example:
     * GET /api/audit?userId=9&action=LOGIN_SUCCESS&resource=auth/login&fromDate=2026-03-10T00:00:00Z&toDate=2026-03-10T23:59:59Z
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate) {

        List<AuditLogResponse> logs = auditLogService.getAuditLogs(userId, action, resource, fromDate, toDate);
        return ResponseEntity.ok(logs);
    }

    /**
     * Admin-only: export audit logs (CSV) with same filters as JSON endpoint.
     * Response has Content-Disposition set for download.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public ResponseEntity<String> exportAuditLogsCsv(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate) {

        String csv = auditLogService.exportAuditLogsCsv(userId, action, resource, fromDate, toDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit_logs.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}