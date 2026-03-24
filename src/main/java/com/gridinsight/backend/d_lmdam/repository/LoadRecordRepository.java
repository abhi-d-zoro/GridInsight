package com.gridinsight.backend.d_lmdam.repository;

import com.gridinsight.backend.d_lmdam.entity.LoadRecord;
import com.gridinsight.backend.d_lmdam.entity.DemandType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public interface LoadRecordRepository extends JpaRepository<LoadRecord, Long> {

    Page<LoadRecord> findByZoneIdAndTimestampBetweenAndDemandTypeIn(
            Long zoneId, Instant from, Instant to, EnumSet<DemandType> types, Pageable pageable);

    Page<LoadRecord> findByZoneIdAndTimestampBetween(
            Long zoneId, Instant from, Instant to, Pageable pageable);

    Page<LoadRecord> findByZoneId(Long zoneId, Pageable pageable);

    // ---- New: Idempotency key query
    Optional<LoadRecord> findByZoneIdAndTimestamp(Long zoneId, Instant timestamp);

    // ---- New: Overlay needs time-ordered series
    List<LoadRecord> findByZoneIdAndTimestampBetweenOrderByTimestamp(Long zoneId, Instant from, Instant to);

    // ---- New: rolling 15-min max
    @Query("select max(l.demandMW) " +
            "from LoadRecord l " +
            "where l.zoneId = :zoneId and l.timestamp between :from and :to")
    Double findMaxDemandInWindow(@Param("zoneId") Long zoneId,
                                 @Param("from") Instant from,
                                 @Param("to") Instant to);
}