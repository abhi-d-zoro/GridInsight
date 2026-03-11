package com.gridinsight.backend.RGMM_3.repository;

import com.gridinsight.backend.RGMM_3.entity.GenerationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GenerationRecordRepository extends JpaRepository<GenerationRecord, Long> {
    Optional<GenerationRecord> findByAssetIdAndTimestamp(String assetId, LocalDateTime timestamp);
}
