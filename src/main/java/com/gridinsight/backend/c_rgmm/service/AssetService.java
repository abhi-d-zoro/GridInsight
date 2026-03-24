package com.gridinsight.backend.c_rgmm.service;

import com.gridinsight.backend.c_rgmm.dto.AssetRequest;
import com.gridinsight.backend.c_rgmm.dto.AssetResponse;
import com.gridinsight.backend.c_rgmm.dto.MaintenanceDTO;
import com.gridinsight.backend.c_rgmm.entity.Asset;
import com.gridinsight.backend.c_rgmm.entity.AssetStatus;
import com.gridinsight.backend.c_rgmm.entity.AssetType;
import com.gridinsight.backend.c_rgmm.exception.DuplicateAssetException;
import com.gridinsight.backend.c_rgmm.exception.InvalidStatusTransitionException;
import com.gridinsight.backend.c_rgmm.repository.AssetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepo;

    @Transactional
    public AssetResponse createAsset(AssetRequest req) {
        if (req.identifier() != null && assetRepo.existsByLocationAndIdentifier(req.location(), req.identifier())) {
            throw new DuplicateAssetException(req.location(), req.identifier());
        }

        Asset asset = Asset.builder()
                .type(AssetType.valueOf(req.type().toUpperCase()))
                .location(req.location())
                .identifier(req.identifier())
                .capacity(req.capacity())
                .commissionDate(req.commissionDate())
                .status(AssetStatus.OPERATIONAL) // default
                .build();

        Asset saved = assetRepo.save(asset);
        return toResponse(saved);
    }

    public Page<AssetResponse> listAssets(Pageable pageable) {
        return assetRepo.findAll(pageable).map(this::toResponse);
    }

    public AssetResponse getAsset(Long id) {
        return assetRepo.findById(id).map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));
    }

    @Transactional
    public AssetResponse updateStatus(Long id, AssetStatus newStatus) {
        Asset asset = assetRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));

        validateTransition(asset.getStatus(), newStatus);
        asset.setStatus(newStatus);

        Asset updated = assetRepo.save(asset);
        return toResponse(updated);
    }

    @Transactional
    public void deleteAsset(Long id) {
        assetRepo.deleteById(id);
    }

    // --- New Feature: Flag asset under maintenance ---
    @Transactional
    public AssetResponse flagUnderMaintenance(Long id, MaintenanceDTO dto) {
        Asset asset = assetRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));

        asset.setStatus(AssetStatus.UNDER_MAINTENANCE);
        asset.setMaintenanceNote(dto.getNote());
        asset.setMaintenanceStart(dto.getStartDate());
        asset.setMaintenanceEnd(dto.getEndDate());

        Asset updated = assetRepo.save(asset);
        return toResponse(updated);
    }

    private void validateTransition(AssetStatus current, AssetStatus next) {
        // Example: allow any transition except OPERATIONAL -> OFFLINE directly
        if (current == AssetStatus.OPERATIONAL && next == AssetStatus.OFFLINE) {
            throw new InvalidStatusTransitionException(current.name(), next.name());
        }
    }

    private AssetResponse toResponse(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getType(),
                asset.getLocation(),
                asset.getIdentifier(),
                asset.getCapacity(),
                asset.getCommissionDate(),
                asset.getStatus(),
                asset.getCreatedAt(),
                asset.getUpdatedAt(),
                asset.getMaintenanceNote(),
                asset.getMaintenanceStart(),
                asset.getMaintenanceEnd()
        );
    }
}
