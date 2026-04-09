package com.gridinsight.backend.e_fgpm.repository;

import com.gridinsight.backend.e_fgpm.entity.ForecastJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ForecastJobRepository extends JpaRepository<ForecastJob, Long> {

    List<ForecastJob> findByZoneId(String zoneId);

    List<ForecastJob> findByTargetDate(LocalDate targetDate);

    List<ForecastJob> findByZoneIdAndTargetDate(String zoneId, LocalDate targetDate);

    // ✅ For accuracy calculation
    Optional<ForecastJob> findFirstByZoneIdAndTargetDateAndStatusOrderByCreatedAtDesc(
            String zoneId, LocalDate targetDate, String status);
}
