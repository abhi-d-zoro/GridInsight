package com.gridinsight.backend.GTMPM_2.service;

import com.gridinsight.backend.GTMPM_2.dto.MeasurementPointResponseDTO;
import com.gridinsight.backend.GTMPM_2.dto.PageResponse;
import com.gridinsight.backend.GTMPM_2.dto.ZoneSummaryDTO;
import com.gridinsight.backend.GTMPM_2.entity.GridZone;
import com.gridinsight.backend.GTMPM_2.repository.GridZoneRepository;
import com.gridinsight.backend.GTMPM_2.repository.MeasurementPointRepository;
import com.gridinsight.backend.z_common.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopologyServiceImpl implements TopologyService {

    private final GridZoneRepository zoneRepo;
    private final MeasurementPointRepository mpRepo;
    private final AuditLogService auditService;

    @Override
    public PageResponse<ZoneSummaryDTO> listZones(int page, int size) {
        int p = Math.max(page, 0);
        int s = (size <= 0 || size > 50) ? 10 : size; // small pages for p95 < 2s
        Pageable pageable = PageRequest.of(p, s, Sort.by("id").ascending());

        Page<GridZone> zones = zoneRepo.findAll(pageable);

        // Corrected audit call
        try {
            auditService.logAction(
                    "READ",
                    null, // actorUserId (can be set from SecurityContext later if needed)
                    null, // targetUserId
                    "TOPOLOGY_ZONES",
                    Map.of("page", p, "size", s)
            );
        } catch (Exception ignored) {}

        return PageResponse.<ZoneSummaryDTO>builder()
                .items(zones.getContent().stream().map(z -> ZoneSummaryDTO.builder()
                        .id(z.getId())
                        .name(z.getName())
                        .region(z.getRegion())
                        .voltageLevel(z.getVoltageLevel())
                        .status(z.getStatus())
                        .pointsCount(mpRepo.countByZone_Id(z.getId()))
                        .build()).collect(Collectors.toList()))
                .page(zones.getNumber())
                .size(zones.getSize())
                .totalElements(zones.getTotalElements())
                .totalPages(zones.getTotalPages())
                .hasNext(zones.hasNext())
                .hasPrevious(zones.hasPrevious())
                .build();
    }

    @Override
    public PageResponse<MeasurementPointResponseDTO> listPointsByZone(Long zoneId, int page, int size, String q) {
        int p = Math.max(page, 0);
        int s = (size <= 0 || size > 100) ? 20 : size;

        Pageable pageable = PageRequest.of(p, s, Sort.by("id").descending());

        var pageResult = (q != null && !q.isBlank())
                ? mpRepo.findByZone_IdAndIdentifierContainingIgnoreCase(zoneId, q.trim(), pageable)
                : mpRepo.findByZone_Id(zoneId, pageable);

        // Corrected audit call
        try {
            auditService.logAction(
                    "READ",
                    null,
                    null,
                    "TOPOLOGY_ZONE_POINTS",
                    Map.of("zoneId", zoneId, "page", p, "size", s, "q", q)
            );
        } catch (Exception ignored) {}

        return PageResponse.<MeasurementPointResponseDTO>builder()
                .items(pageResult.map(mp -> MeasurementPointResponseDTO.builder()
                        .id(mp.getId())
                        .zoneId(mp.getZone().getId())
                        .zoneName(mp.getZone().getName())
                        .assetType(mp.getAssetType())
                        .identifier(mp.getIdentifier())
                        .unit(mp.getUnit())
                        .status(mp.getStatus())
                        .createdAt(mp.getCreatedAt())
                        .updatedAt(mp.getUpdatedAt())
                        .build()).getContent())
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
    }
}
