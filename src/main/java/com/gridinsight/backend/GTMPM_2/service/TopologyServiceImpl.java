package com.gridinsight.backend.GTMPM_2.service;

import com.gridinsight.backend.GTMPM_2.dto.PageResponse;
import com.gridinsight.backend.GTMPM_2.dto.ZoneSummaryDTO;
import com.gridinsight.backend.GTMPM_2.dto.MeasurementPointResponseDTO;
import com.gridinsight.backend.GTMPM_2.entity.GridZone;
import com.gridinsight.backend.GTMPM_2.repository.GridZoneRepository;
import com.gridinsight.backend.repository.MeasurementPointRepository;
import com.gridinsight.backend.z_common.audit.AuditLogService; // <-- your existing audit service
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopologyServiceImpl implements TopologyService {

    private final GridZoneRepository zoneRepo;
    private final MeasurementPointRepository mpRepo;
    private final AuditLogService auditService; // <-- uses your type

    @Override
    public PageResponse<ZoneSummaryDTO> listZones(int page, int size) {
        int p = Math.max(page, 0);
        int s = (size <= 0 || size > 50) ? 10 : size; // small page size helps p95 < 2s
        Pageable pageable = PageRequest.of(p, s, Sort.by("id").ascending());

        Page<GridZone> zones = zoneRepo.findAll(pageable);

        // ---- Batch count points per zone in a single query (performance fix) ----
        List<Long> zoneIds = zones.getContent().stream()
                .map(GridZone::getId)
                .collect(Collectors.toList());

        Map<Long, Long> counts = new HashMap<>();
        if (!zoneIds.isEmpty()) {
            for (Object[] row : mpRepo.countByZoneIds(zoneIds)) {
                Long zoneId = (Long) row[0];
                Long cnt = (Long) row[1];
                counts.put(zoneId, cnt);
            }
        }

        try {
            // ✳✳✳ CHANGE THIS to whatever your AuditLogService exposes ✳✳✳
            // Examples:
            // auditService.record("READ", "TOPOLOGY_ZONES", String.format("PAGE=%d SIZE=%d", p, s));
            // auditService.save("READ", "TOPOLOGY_ZONES", String.format("PAGE=%d SIZE=%d", p, s));
            // auditService.logEvent("READ", "TOPOLOGY_ZONES", String.format("PAGE=%d SIZE=%d", p, s));
            auditService.record("READ", "TOPOLOGY_ZONES", String.format("PAGE=%d SIZE=%d", p, s)); // <<< CHANGE THIS
        } catch (Exception ignored) {}

        return PageResponse.<ZoneSummaryDTO>builder()
                .items(zones.getContent().stream().map(z -> ZoneSummaryDTO.builder()
                        .id(z.getId())
                        .name(z.getName())
                        .region(z.getRegion())
                        .voltageLevel(z.getVoltageLevel())
                        .status(z.getStatus())
                        .pointsCount(counts.getOrDefault(z.getId(), 0L))
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

        try {
            // ✳✳✳ CHANGE THIS to whatever your AuditLogService exposes ✳✳✳
            // Examples:
            // auditService.record("READ", "TOPOLOGY_ZONE_POINTS", String.format("ZONE=%d PAGE=%d SIZE=%d Q=%s", zoneId, p, s, q));
            // auditService.save("READ", "TOPOLOGY_ZONE_POINTS", String.format("ZONE=%d PAGE=%d SIZE=%d Q=%s", zoneId, p, s, q));
            // auditService.logEvent("READ", "TOPOLOGY_ZONE_POINTS", String.format("ZONE=%d PAGE=%d SIZE=%d Q=%s", zoneId, p, s, q));
            auditService.record("READ", "TOPOLOGY_ZONE_POINTS",
                    String.format("ZONE=%d PAGE=%d SIZE=%d Q=%s", zoneId, p, s, q)); // <<< CHANGE THIS
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