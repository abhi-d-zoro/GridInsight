package com.gridinsight.backend.repository;

import com.gridinsight.backend.GTMPM_2.entity.MeasurementPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MeasurementPointRepository extends JpaRepository<MeasurementPoint, Long> {

    boolean existsByZone_IdAndIdentifier(Long zoneId, String identifier);

    boolean existsByZone_IdAndIdentifierAndIdNot(Long zoneId, String identifier, Long id);

    Page<MeasurementPoint> findByZone_Id(Long zoneId, Pageable pageable);

    Page<MeasurementPoint> findByZone_IdAndIdentifierContainingIgnoreCase(Long zoneId, String identifier, Pageable pageable);

    Page<MeasurementPoint> findByIdentifierContainingIgnoreCase(String identifier, Pageable pageable);

    long countByZone_Id(Long zoneId);

    /**
     * Batch count points for many zones in a single query.
     * Returns rows as Object[]: [0] = zoneId (Long), [1] = count (Long).
     */
    @Query("select mp.zone.id as zoneId, count(mp) as cnt " +
            "from MeasurementPoint mp " +
            "where mp.zone.id in :zoneIds " +
            "group by mp.zone.id")
    List<Object[]> countByZoneIds(@Param("zoneIds") Collection<Long> zoneIds);
}