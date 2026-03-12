package com.gridinsight.backend.LMDAM_4.repository;

import com.gridinsight.backend.LMDAM_4.entity.PeakEvent;
import com.gridinsight.backend.LMDAM_4.entity.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PeakEventRepository extends JpaRepository<PeakEvent, Long> {

    Page<PeakEvent> findByZoneId(Long zoneId, Pageable pageable);

    Page<PeakEvent> findByZoneIdAndSeverity(Long zoneId, Severity severity, Pageable pageable);

    // ---- New: get the latest peak for a zone to extend/update when breach continues
    Optional<PeakEvent> findTopByZoneIdOrderByEndTimeDesc(Long zoneId);
}