package com.gridinsight.backend.b_gtmpm.service;

import com.gridinsight.backend.b_gtmpm.dto.GridZoneRequestDTO;
import com.gridinsight.backend.b_gtmpm.dto.GridZoneResponseDTO;

import java.util.List;

public interface GridZoneService {

    GridZoneResponseDTO create(GridZoneRequestDTO request);

    GridZoneResponseDTO update(Long id, GridZoneRequestDTO request);

    void delete(Long id);

    GridZoneResponseDTO getById(Long id);

    List<GridZoneResponseDTO> getAll();
}