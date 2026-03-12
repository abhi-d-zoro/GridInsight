package com.gridinsight.backend.GTMPM_2.service;

import com.gridinsight.backend.GTMPM_2.dto.GridZoneRequestDTO;
import com.gridinsight.backend.GTMPM_2.dto.GridZoneResponseDTO;

import java.util.List;

public interface GridZoneService {

    GridZoneResponseDTO create(GridZoneRequestDTO request);

    GridZoneResponseDTO update(Long id, GridZoneRequestDTO request);

    void delete(Long id);

    GridZoneResponseDTO getById(Long id);

    List<GridZoneResponseDTO> getAll();
}