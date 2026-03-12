package com.gridinsight.backend.GTMPM_2.service;

import com.gridinsight.backend.GTMPM_2.dto.MeasurementPointResponseDTO;
import com.gridinsight.backend.GTMPM_2.dto.PageResponse;
import com.gridinsight.backend.GTMPM_2.dto.ZoneSummaryDTO;

public interface TopologyService {

    PageResponse<ZoneSummaryDTO> listZones(int page, int size);

    PageResponse<MeasurementPointResponseDTO> listPointsByZone(Long zoneId, int page, int size, String q);
}