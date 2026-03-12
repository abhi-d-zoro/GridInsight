package com.gridinsight.backend.GTMPM_2.repository;

import com.gridinsight.backend.GTMPM_2.entity.GridZone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GridZoneRepository extends JpaRepository<GridZone, Long> {

    boolean existsByNameAndRegion(String name, String region);

    // For PUT: same name+region in another row (exclude current id)
    boolean existsByNameAndRegionAndIdNot(String name, String region, Long id);
}