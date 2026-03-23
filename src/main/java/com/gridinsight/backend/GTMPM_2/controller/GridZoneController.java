package com.gridinsight.backend.GTMPM_2.controller;

import com.gridinsight.backend.GTMPM_2.dto.GridZoneRequestDTO;
import com.gridinsight.backend.GTMPM_2.dto.GridZoneResponseDTO;
import com.gridinsight.backend.GTMPM_2.service.GridZoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/grid-zones")
@RequiredArgsConstructor
public class GridZoneController {

    private final GridZoneService service;

    @PostMapping
    public GridZoneResponseDTO create(@Valid @RequestBody GridZoneRequestDTO request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public GridZoneResponseDTO update(@PathVariable Long id,
                                      @Valid @RequestBody GridZoneRequestDTO request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public GridZoneResponseDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    public List<GridZoneResponseDTO> getAll() {
        return service.getAll();
    }
}