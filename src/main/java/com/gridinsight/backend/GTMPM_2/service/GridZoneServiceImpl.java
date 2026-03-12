package com.gridinsight.backend.GTMPM_2.service;

import com.gridinsight.backend.GTMPM_2.dto.GridZoneRequestDTO;
import com.gridinsight.backend.GTMPM_2.dto.GridZoneResponseDTO;
import com.gridinsight.backend.GTMPM_2.entity.GridZone;
import com.gridinsight.backend.GTMPM_2.repository.GridZoneRepository;
import com.gridinsight.backend.z_common.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class GridZoneServiceImpl implements GridZoneService {

    private final GridZoneRepository repository;
    private final AuditLogService auditService;

    // ================= CREATE =================
    @Override
    public GridZoneResponseDTO create(GridZoneRequestDTO request) {

        // Enforce unique (name + region)
        if (repository.existsByNameAndRegion(request.getName(), request.getRegion())) {
            throw new IllegalStateException("Grid Zone already exists with same Name and Region");
        }

        GridZone zone = GridZone.builder()
                .name(request.getName())
                .region(request.getRegion())
                .voltageLevel(request.getVoltageLevel())
                .status(request.getStatus())
                .build();

        GridZone saved = repository.save(zone);

        // Audit log (action, actorUserId=null, targetUserId=zoneId, resource, metadata)
        try {
            auditService.logAction(
                    "CREATE",
                    null,
                    saved.getId(),
                    "GRID_ZONE",
                    Map.of(
                            "name", saved.getName(),
                            "region", saved.getRegion(),
                            "voltageLevel", saved.getVoltageLevel(),
                            "status", saved.getStatus()
                    )
            );
        } catch (Exception ignored) { }

        return mapToDTO(saved);
    }

    // ================= UPDATE =================
    @Override
    public GridZoneResponseDTO update(Long id, GridZoneRequestDTO request) {

        GridZone zone = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Grid Zone not found"));

        boolean nameChanged = !zone.getName().equals(request.getName());
        boolean regionChanged = !zone.getRegion().equals(request.getRegion());

        if (nameChanged || regionChanged) {
            // Exclude the current record from duplicate check
            if (repository.existsByNameAndRegionAndIdNot(request.getName(), request.getRegion(), id)) {
                throw new IllegalStateException("Grid Zone already exists with same Name and Region");
            }
        }

        zone.setName(request.getName());
        zone.setRegion(request.getRegion());
        zone.setVoltageLevel(request.getVoltageLevel());
        zone.setStatus(request.getStatus());

        GridZone updated = repository.save(zone);

        // Audit log: capture changed fields (simple delta)
        try {
            auditService.logAction(
                    "UPDATE",
                    null,
                    updated.getId(),
                    "GRID_ZONE",
                    Map.of(
                            "name", updated.getName(),
                            "region", updated.getRegion(),
                            "voltageLevel", updated.getVoltageLevel(),
                            "status", updated.getStatus()
                    )
            );
        } catch (Exception ignored) { }

        return mapToDTO(updated);
    }

    // ================= DELETE =================
    @Override
    public void delete(Long id) {
        GridZone zone = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Grid Zone not found"));

        repository.delete(zone);

        // Audit log
        try {
            auditService.logAction(
                    "DELETE",
                    null,
                    id,
                    "GRID_ZONE",
                    Map.of(
                            "name", zone.getName(),
                            "region", zone.getRegion()
                    )
            );
        } catch (Exception ignored) { }
    }

    // ================= GET BY ID =================
    @Override
    public GridZoneResponseDTO getById(Long id) {
        GridZone zone = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Grid Zone not found"));

        // Audit log
        try {
            auditService.logAction(
                    "READ",
                    null,
                    id,
                    "GRID_ZONE",
                    Map.of("id", id)
            );
        } catch (Exception ignored) { }

        return mapToDTO(zone);
    }

    // ================= GET ALL =================
    @Override
    public List<GridZoneResponseDTO> getAll() {
        try {
            auditService.logAction(
                    "READ",
                    null,
                    null,
                    "GRID_ZONE",
                    Map.of("scope", "ALL")
            );
        } catch (Exception e) {
            // Prevent audit failure from crashing the endpoint
            System.out.println("Audit failed for READ ALL: " + e.getMessage());
        }

        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // ================= MAPPER =================
    private GridZoneResponseDTO mapToDTO(GridZone zone) {
        return GridZoneResponseDTO.builder()
                .id(zone.getId())
                .name(zone.getName())
                .region(zone.getRegion())
                .voltageLevel(zone.getVoltageLevel())
                .status(zone.getStatus())
                .build();
    }
}
