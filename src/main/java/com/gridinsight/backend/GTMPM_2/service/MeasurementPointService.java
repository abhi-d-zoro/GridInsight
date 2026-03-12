package com.gridinsight.backend.GTMPM_2.service;

import com.gridinsight.backend.GTMPM_2.dto.MeasurementPointRequestDTO;
import com.gridinsight.backend.GTMPM_2.dto.MeasurementPointResponseDTO;
import com.gridinsight.backend.GTMPM_2.dto.PageResponse;

public interface MeasurementPointService {

    MeasurementPointResponseDTO create(MeasurementPointRequestDTO request);

    MeasurementPointResponseDTO update(Long id, MeasurementPointRequestDTO request);

    void delete(Long id);

    MeasurementPointResponseDTO getById(Long id);

    PageResponse<MeasurementPointResponseDTO> getAll(Integer page, Integer size, String q, Long zoneId);
}