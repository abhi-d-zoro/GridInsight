package com.gridinsight.backend.LMDAM_4.controller;

import com.gridinsight.backend.IAM_1.entity.User;
import com.gridinsight.backend.LMDAM_4.dto.*;
import com.gridinsight.backend.LMDAM_4.entity.DemandType;
import com.gridinsight.backend.LMDAM_4.entity.Severity;
import com.gridinsight.backend.LMDAM_4.service.LoadMonitoringService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.EnumSet;

@RestController
@RequestMapping("/load")
@RequiredArgsConstructor
public class LoadMonitoringController {

    private final LoadMonitoringService service;

    private Long currentUserId(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User u) {
            return u.getId();
        }
        return null;
    }

    // ===== Overlay (Demand vs Generated) =====
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/overlay")
    public ResponseEntity<LoadMonitoringService.OverlayResponse> overlay(
            @RequestParam Long zoneId,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(required = false) LoadMonitoringService.Granularity granularity) {
        return ResponseEntity.ok(service.getOverlay(zoneId, from, to, granularity));
    }

    // ===== LoadRecord =====

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/records")
    public ResponseEntity<LoadRecordResponse> createLoadRecord(
            @Valid @RequestBody LoadRecordCreateRequest req,
            Authentication auth,
            HttpServletRequest request) {
        var res = service.createLoadRecord(req, currentUserId(auth), request.getRemoteAddr());
        return ResponseEntity.status(201).body(res);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/records")
    public ResponseEntity<Page<LoadRecordResponse>> listLoadRecords(
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) EnumSet<DemandType> types,
            Pageable pageable) {
        return ResponseEntity.ok(service.listLoadRecords(zoneId, from, to, types, pageable));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/records/{id}")
    public ResponseEntity<LoadRecordResponse> getLoadRecord(@PathVariable Long id) {
        return ResponseEntity.ok(service.getLoadRecord(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/records/{id}")
    public ResponseEntity<LoadRecordResponse> updateLoadRecord(
            @PathVariable Long id,
            @Valid @RequestBody LoadRecordUpdateRequest req,
            Authentication auth,
            HttpServletRequest request) {
        return ResponseEntity.ok(service.updateLoadRecord(id, req, currentUserId(auth), request.getRemoteAddr()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/records/{id}")
    public ResponseEntity<Void> deleteLoadRecord(
            @PathVariable Long id,
            Authentication auth,
            HttpServletRequest request) {
        service.deleteLoadRecord(id, currentUserId(auth), request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    // ===== PeakEvent =====

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/peaks")
    public ResponseEntity<PeakEventResponse> createPeak(
            @Valid @RequestBody PeakEventCreateRequest req,
            Authentication auth,
            HttpServletRequest request) {
        var res = service.createPeak(req, currentUserId(auth), request.getRemoteAddr());
        return ResponseEntity.status(201).body(res);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/peaks")
    public ResponseEntity<Page<PeakEventResponse>> listPeaks(
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Severity severity,
            Pageable pageable) {
        return ResponseEntity.ok(service.listPeaks(zoneId, severity, pageable));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/peaks/{id}")
    public ResponseEntity<PeakEventResponse> getPeak(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPeak(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/peaks/{id}")
    public ResponseEntity<PeakEventResponse> updatePeak(
            @PathVariable Long id,
            @Valid @RequestBody PeakEventUpdateRequest req,
            Authentication auth,
            HttpServletRequest request) {
        return ResponseEntity.ok(service.updatePeak(id, req, currentUserId(auth), request.getRemoteAddr()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/peaks/{id}")
    public ResponseEntity<Void> deletePeak(
            @PathVariable Long id,
            Authentication auth,
            HttpServletRequest request) {
        service.deletePeak(id, currentUserId(auth), request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}