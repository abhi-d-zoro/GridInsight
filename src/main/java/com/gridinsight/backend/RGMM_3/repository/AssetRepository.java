package com.gridinsight.backend.RGMM_3.repository;

import com.gridinsight.backend.RGMM_3.entity.Asset;
import com.gridinsight.backend.RGMM_3.entity.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    // Duplicate prevention
    boolean existsByLocationAndIdentifier(String location, String identifier);
    Optional<Asset> findByLocationAndIdentifier(String location, String identifier);

    // Fetch assets by status
    List<Asset> findByStatus(AssetStatus status);

    // Fetch assets under maintenance within a date range
    List<Asset> findByStatusAndMaintenanceStartLessThanEqualAndMaintenanceEndGreaterThanEqual(
            AssetStatus status,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate
    );
}
