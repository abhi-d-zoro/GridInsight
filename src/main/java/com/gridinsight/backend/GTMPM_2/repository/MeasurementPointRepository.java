package com.gridinsight.backend.GTMPM_2.repository;

import com.gridinsight.backend.GTMPM_2.entity.MeasurementPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementPointRepository extends JpaRepository<MeasurementPoint, Long> {

    boolean existsByZone_IdAndIdentifier(Long zoneId, String identifier);

    boolean existsByZone_IdAndIdentifierAndIdNot(Long zoneId, String identifier, Long id);

    Page<MeasurementPoint> findByZone_Id(Long zoneId, Pageable pageable);

    Page<MeasurementPoint> findByZone_IdAndIdentifierContainingIgnoreCase(Long zoneId, String identifier, Pageable pageable);

    Page<MeasurementPoint> findByIdentifierContainingIgnoreCase(String identifier, Pageable pageable);

    long countByZone_Id(Long zoneId);
}