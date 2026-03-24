package com.gridinsight.backend.c_rgmm.controller;

import com.gridinsight.backend.c_rgmm.service.GenerationRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/generation")
@RequiredArgsConstructor
public class GenerationRecordController {

    private final GenerationRecordService service;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ASSET_MANAGER','ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadCsv(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.uploadCsv(file));
    }
}
