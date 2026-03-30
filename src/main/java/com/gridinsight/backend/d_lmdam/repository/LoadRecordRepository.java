package com.gridinsight.backend.d_lmdam.repository;

import com.gridinsight.backend.d_lmdam.entity.DemandType;
import com.gridinsight.backend.d_lmdam.entity.LoadRecord;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public interface LoadRecordRepository extends JpaRepository<LoadRecord, Long> {

    Page<LoadRecord> findByZoneIdAndTimestampBetweenAndDemandTypeIn(
            String zoneId, Instant from, Instant to, EnumSet<DemandType> types, Pageable pageable);

    Page<LoadRecord> findByZoneIdAndTimestampBetween(
            String zoneId, Instant from, Instant to, Pageable pageable);

    Page<LoadRecord> findByZoneId(String zoneId, Pageable pageable);

    Optional<LoadRecord> findByZoneIdAndTimestamp(String zoneId, Instant timestamp);

    List<LoadRecord> findByZoneIdAndTimestampBetweenOrderByTimestamp(String zoneId, Instant from, Instant to);

    @Query("""
        select max(l.demandMW)
        from LoadRecord l
        where l.zoneId = :zoneId and l.timestamp between :from and :to
    """)
    Double findMaxDemandInWindow(@Param("zoneId") String zoneId,
                                 @Param("from") Instant from,
                                 @Param("to") Instant to);
}