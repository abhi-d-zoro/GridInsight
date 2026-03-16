package com.gridinsight.backend.SERM_6.repository;

import com.gridinsight.backend.SERM_6.entity.EnergyData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnergyDataRepository extends JpaRepository<EnergyData, Long> {
    Optional<EnergyData> findByPeriod(String period);
}
