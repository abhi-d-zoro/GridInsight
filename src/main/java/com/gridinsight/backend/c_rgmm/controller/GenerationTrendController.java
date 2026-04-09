package com.gridinsight.backend.c_rgmm.controller;

import com.gridinsight.backend.c_rgmm.dto.TrendResponseDto;
import com.gridinsight.backend.c_rgmm.service.GenerationTrendService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/generation/trends")
@RequiredArgsConstructor
public class GenerationTrendController {

    private final GenerationTrendService service;

    @GetMapping
    //changed by patil
    @PreAuthorize("hasAnyRole('GRID_ANALYST','ADMIN','ASSET_MANAGER')")
    public ResponseEntity<TrendResponseDto> getTrends(
            @RequestParam String assetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(service.getTrends(assetId, start, end));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('GRID_ANALYST','ADMIN')")
    public ResponseEntity<byte[]> export(
            @RequestParam String assetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam String format) {

        TrendResponseDto dto = service.getTrends(assetId, start, end);

        if ("csv".equalsIgnoreCase(format)) {
            byte[] data = service.exportCsv(dto);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=trends.csv")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(data);
        } else if ("png".equalsIgnoreCase(format)) {
            byte[] data = service.exportPng(dto);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=trends.png")
                    .contentType(MediaType.IMAGE_PNG)
                    .body(data);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
