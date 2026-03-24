package com.gridinsight.backend.c_rgmm.repository;

import com.gridinsight.backend.c_rgmm.entity.GenerationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GenerationRecordRepository extends JpaRepository<GenerationRecord, Long> {
    Optional<GenerationRecord> findByAssetIdAndTimestamp(String assetId, LocalDateTime timestamp);
}
