package com.gridinsight.backend.e_fgpm.repository;

import com.gridinsight.backend.e_fgpm.entity.MonthForecastRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonthForecastRepository extends JpaRepository<MonthForecastRecord, Long> {
    List<MonthForecastRecord> findByAssetTypeOrderByForecastDateAsc(String assetType);
}
