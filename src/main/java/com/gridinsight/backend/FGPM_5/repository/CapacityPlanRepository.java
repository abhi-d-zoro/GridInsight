package com.gridinsight.backend.FGPM_5.repository;

import com.gridinsight.backend.FGPM_5.entity.CapacityPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CapacityPlanRepository extends JpaRepository<CapacityPlan, Long> {
    // Finds the latest version of a plan for a specific zone
    Optional<CapacityPlan> findTopByZoneIdOrderByPlanVersionDesc(String zoneId);
}