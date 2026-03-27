package com.gridinsight.backend.e_fgpm.repository;

import com.gridinsight.backend.e_fgpm.entity.ForecastJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ForecastJobRepository extends JpaRepository<ForecastJob, Long> {

    Optional<ForecastJob> findFirstByZoneIdAndTargetDateAndStatusOrderByCreatedAtDesc(
            String zoneId,
            LocalDateTime targetDate,
            String status
    );
}