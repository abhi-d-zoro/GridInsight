package com.gridinsight.backend.b_gtmpm.controller;

import com.gridinsight.backend.b_gtmpm.dto.MeasurementPointResponseDTO;
import com.gridinsight.backend.b_gtmpm.dto.PageResponse;
import com.gridinsight.backend.b_gtmpm.dto.ZoneSummaryDTO;
import com.gridinsight.backend.b_gtmpm.service.TopologyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topology")
@RequiredArgsConstructor
public class TopologyController {

    private final TopologyService topologyService;

    // Zones list with counts, paginated
    @GetMapping("/zones")
    public PageResponse<ZoneSummaryDTO> zones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return topologyService.listZones(page, size);
    }

    // Points under a zone, paginated + search by identifier
    @GetMapping("/zones/{zoneId}/points")
    public PageResponse<MeasurementPointResponseDTO> zonePoints(
            @PathVariable Long zoneId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q) {
        return topologyService.listPointsByZone(zoneId, page, size, q);
    }
}