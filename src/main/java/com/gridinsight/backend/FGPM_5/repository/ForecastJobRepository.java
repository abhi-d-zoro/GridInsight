package com.gridinsight.backend.FGPM_5.repository;

import com.gridinsight.backend.FGPM_5.entity.ForecastJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ForecastJobRepository extends JpaRepository<ForecastJob, Long> {

    // UPDATED: Grabs the most recent forecast if the user ran multiple forecasts for the same day
    Optional<ForecastJob> findFirstByZoneIdAndTargetDateAndStatusOrderByCreatedAtDesc(String zoneId, LocalDateTime targetDate, String status);
}