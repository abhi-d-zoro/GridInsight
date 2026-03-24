package com.gridinsight.backend.b_gtmpm.service;

import com.gridinsight.backend.b_gtmpm.dto.MeasurementPointRequestDTO;
import com.gridinsight.backend.b_gtmpm.dto.MeasurementPointResponseDTO;
import com.gridinsight.backend.b_gtmpm.dto.PageResponse;
import com.gridinsight.backend.b_gtmpm.entity.GridZone;
import com.gridinsight.backend.b_gtmpm.entity.MeasurementPoint;
import com.gridinsight.backend.b_gtmpm.entity.PointStatus;
import com.gridinsight.backend.b_gtmpm.repository.GridZoneRepository;
import com.gridinsight.backend.repository.MeasurementPointRepository;
import com.gridinsight.backend.z_common.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MeasurementPointServiceImpl implements MeasurementPointService {

    private final MeasurementPointRepository repository;
    private final GridZoneRepository gridZoneRepository;

    // Use the centralized audit service from z_common
    private final AuditLogService auditService;

    // ================= ALLOWED STATUS TRANSITIONS =================
    private static final Map<PointStatus, Set<PointStatus>> ALLOWED = Map.of(
            PointStatus.INACTIVE, EnumSet.of(PointStatus.ACTIVE, PointStatus.DECOMMISSIONED),
            PointStatus.ACTIVE, EnumSet.of(PointStatus.INACTIVE, PointStatus.MAINTENANCE, PointStatus.DECOMMISSIONED),
            PointStatus.MAINTENANCE, EnumSet.of(PointStatus.ACTIVE, PointStatus.INACTIVE, PointStatus.DECOMMISSIONED),
            PointStatus.DECOMMISSIONED, EnumSet.noneOf(PointStatus.class)
    );

    // ================= CREATE =================
    @Override
    public MeasurementPointResponseDTO create(MeasurementPointRequestDTO request) {
        GridZone zone = gridZoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new NoSuchElementException("Grid Zone not found"));

        if (repository.existsByZone_IdAndIdentifier(request.getZoneId(), request.getIdentifier())) {
            throw new IllegalStateException("Identifier already exists in the Zone");
        }

        MeasurementPoint point = MeasurementPoint.builder()
                .zone(zone)
                .assetType(request.getAssetType())
                .identifier(request.getIdentifier())
                .unit(request.getUnit())
                .status(request.getStatus())
                .build();

        MeasurementPoint saved = repository.save(point);

        // AUDIT
        try {
            auditService.logAction(
                    "CREATE",
                    null,                         // actorUserId (can be auto-set inside service later)
                    saved.getId(),                // targetUserId
                    "MEASUREMENT_POINT",
                    Map.of(
                            "zoneId", saved.getZone().getId(),
                            "assetType", saved.getAssetType(),
                            "identifier", saved.getIdentifier(),
                            "unit", saved.getUnit(),
                            "status", saved.getStatus()
                    )
            );
        } catch (Exception ignored) {}

        return map(saved);
    }

    // ================= UPDATE =================
    @Override
    public MeasurementPointResponseDTO update(Long id, MeasurementPointRequestDTO request) {
        MeasurementPoint existing = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Measurement Point not found"));

        GridZone targetZone = gridZoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new NoSuchElementException("Grid Zone not found"));

        boolean zoneChanged = !Objects.equals(existing.getZone().getId(), request.getZoneId());
        boolean identifierChanged = !existing.getIdentifier().equals(request.getIdentifier());
        if (zoneChanged || identifierChanged) {
            if (repository.existsByZone_IdAndIdentifierAndIdNot(request.getZoneId(), request.getIdentifier(), id)) {
                throw new IllegalStateException("Identifier already exists in the Zone");
            }
        }

        if (existing.getStatus() != request.getStatus()) {
            validateTransition(existing.getStatus(), request.getStatus());
        }

        existing.setZone(targetZone);
        existing.setAssetType(request.getAssetType());
        existing.setIdentifier(request.getIdentifier());
        existing.setUnit(request.getUnit());
        existing.setStatus(request.getStatus());

        MeasurementPoint updated = repository.save(existing);

        // AUDIT
        try {
            auditService.logAction(
                    "UPDATE",
                    null,
                    updated.getId(),
                    "MEASUREMENT_POINT",
                    Map.of(
                            "zoneId", updated.getZone().getId(),
                            "assetType", updated.getAssetType(),
                            "identifier", updated.getIdentifier(),
                            "unit", updated.getUnit(),
                            "status", updated.getStatus()
                    )
            );
        } catch (Exception ignored) {}

        return map(updated);
    }

    // ================= DELETE =================
    @Override
    public void delete(Long id) {
        MeasurementPoint point = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Measurement Point not found"));
        repository.delete(point);

        // AUDIT
        try {
            auditService.logAction(
                    "DELETE",
                    null,
                    id,
                    "MEASUREMENT_POINT",
                    Map.of(
                            "zoneId", point.getZone().getId(),
                            "identifier", point.getIdentifier()
                    )
            );
        } catch (Exception ignored) {}
    }

    // ================= GET BY ID =================
    @Override
    public MeasurementPointResponseDTO getById(Long id) {
        MeasurementPoint mp = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Measurement Point not found"));

        // AUDIT
        try {
            auditService.logAction(
                    "READ",
                    null,
                    id,
                    "MEASUREMENT_POINT",
                    Map.of("id", id)
            );
        } catch (Exception ignored) {}

        return map(mp);
    }

    // ================= GET ALL (paged + search) =================
    @Override
    public PageResponse<MeasurementPointResponseDTO> getAll(Integer page, Integer size, String q, Long zoneId) {
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0 || size > 100) ? 20 : size;
        Pageable pageable = PageRequest.of(p, s, Sort.by("id").descending());

        Page<MeasurementPoint> result;
        if (zoneId != null && q != null && !q.isBlank()) {
            result = repository.findByZone_IdAndIdentifierContainingIgnoreCase(zoneId, q.trim(), pageable);
        } else if (zoneId != null) {
            result = repository.findByZone_Id(zoneId, pageable);
        } else if (q != null && !q.isBlank()) {
            result = repository.findByIdentifierContainingIgnoreCase(q.trim(), pageable);
        } else {
            result = repository.findAll(pageable);
        }

        // AUDIT
        try {
            auditService.logAction(
                    "READ",
                    null,
                    null,
                    "MEASUREMENT_POINT",
                    Map.of(
                            "page", p,
                            "size", s,
                            "q", q,
                            "zoneId", zoneId
                    )
            );
        } catch (Exception ignored) {}

        return PageResponse.<MeasurementPointResponseDTO>builder()
                .items(result.map(this::map).getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.hasNext())
                .hasPrevious(result.hasPrevious())
                .build();
    }

    // ================= Helpers =================
    private void validateTransition(PointStatus from, PointStatus to) {
        Set<PointStatus> allowed = ALLOWED.getOrDefault(from, EnumSet.noneOf(PointStatus.class));
        if (!allowed.contains(to)) {
            throw new IllegalStateException("Invalid status transition: " + from + " -> " + to);
        }
    }

    private MeasurementPointResponseDTO map(MeasurementPoint mp) {
        return MeasurementPointResponseDTO.builder()
                .id(mp.getId())
                .zoneId(mp.getZone().getId())
                .zoneName(mp.getZone().getName())
                .assetType(mp.getAssetType())
                .identifier(mp.getIdentifier())
                .unit(mp.getUnit())
                .status(mp.getStatus())
                .createdAt(mp.getCreatedAt())
                .updatedAt(mp.getUpdatedAt())
                .build();
    }
}