package com.gridinsight.backend.f_serm.repository;

import com.gridinsight.backend.f_serm.entity.EnergyData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnergyDataRepository extends JpaRepository<EnergyData, Long> {
    Optional<EnergyData> findByPeriod(String period);
}
