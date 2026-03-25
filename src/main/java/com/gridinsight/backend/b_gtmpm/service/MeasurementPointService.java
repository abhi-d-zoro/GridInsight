package com.gridinsight.backend.b_gtmpm.service;

import com.gridinsight.backend.b_gtmpm.dto.MeasurementPointRequestDTO;
import com.gridinsight.backend.b_gtmpm.dto.MeasurementPointResponseDTO;
import com.gridinsight.backend.z_common.util.PageResponse;

public interface MeasurementPointService {

    MeasurementPointResponseDTO create(MeasurementPointRequestDTO request);

    MeasurementPointResponseDTO update(Long id, MeasurementPointRequestDTO request);

    void delete(Long id);

    MeasurementPointResponseDTO getById(Long id);

    PageResponse<MeasurementPointResponseDTO> getAll(Integer page, Integer size, String q, Long zoneId);
}