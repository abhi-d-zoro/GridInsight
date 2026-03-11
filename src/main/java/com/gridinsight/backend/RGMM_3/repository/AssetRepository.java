package com.gridinsight.backend.RGMM_3.repository;

import com.gridinsight.backend.RGMM_3.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    boolean existsByLocationAndIdentifier(String location, String identifier);
    Optional<Asset> findByLocationAndIdentifier(String location, String identifier);
}
