package com.gridinsight.backend.b_gtmpm.service;

import com.gridinsight.backend.b_gtmpm.dto.MeasurementPointResponseDTO;
import com.gridinsight.backend.b_gtmpm.dto.PageResponse;
import com.gridinsight.backend.b_gtmpm.dto.ZoneSummaryDTO;

public interface TopologyService {

    PageResponse<ZoneSummaryDTO> listZones(int page, int size);

    PageResponse<MeasurementPointResponseDTO> listPointsByZone(Long zoneId, int page, int size, String q);
}