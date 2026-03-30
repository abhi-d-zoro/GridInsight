package com.gridinsight.backend.d_lmdam.repository;

import com.gridinsight.backend.d_lmdam.entity.PeakEvent;
import com.gridinsight.backend.d_lmdam.entity.Severity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PeakEventRepository extends JpaRepository<PeakEvent, Long> {

    Page<PeakEvent> findByZoneId(String zoneId, Pageable pageable);

    Page<PeakEvent> findByZoneIdAndSeverity(String zoneId, Severity severity, Pageable pageable);

    Optional<PeakEvent> findTopByZoneIdOrderByEndTimeDesc(String zoneId);
}