package com.gridinsight.backend.c_rgmm.controller;

import com.gridinsight.backend.c_rgmm.dto.AssetRequest;
import com.gridinsight.backend.c_rgmm.dto.AssetResponse;
import com.gridinsight.backend.c_rgmm.dto.MaintenanceDTO;
import com.gridinsight.backend.c_rgmm.entity.AssetStatus;
import com.gridinsight.backend.c_rgmm.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PreAuthorize("hasRole('ASSET_MANAGER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AssetResponse> create(@Valid @RequestBody AssetRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.createAsset(req));
    }

    @PreAuthorize("hasRole('ASSET_MANAGER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<AssetResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(assetService.listAssets(pageable));
    }

    @PreAuthorize("hasRole('ASSET_MANAGER') or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<AssetResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.getAsset(id));
    }

    @PreAuthorize("hasRole('ASSET_MANAGER') or hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<AssetResponse> updateStatus(@PathVariable Long id,
                                                      @RequestParam AssetStatus status) {
        return ResponseEntity.ok(assetService.updateStatus(id, status));
    }

    @PreAuthorize("hasRole('ASSET_MANAGER') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }

    // --- New Feature: Flag asset under maintenance ---
    @PreAuthorize("hasRole('ASSET_MANAGER')")
    @PutMapping("/{id}/maintenance")
    public ResponseEntity<AssetResponse> flagMaintenance(@PathVariable Long id,
                                                         @RequestBody MaintenanceDTO dto) {
        return ResponseEntity.ok(assetService.flagUnderMaintenance(id, dto));
    }
}
