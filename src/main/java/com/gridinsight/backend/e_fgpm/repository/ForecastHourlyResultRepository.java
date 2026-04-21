package com.gridinsight.backend.e_fgpm.repository;

import com.gridinsight.backend.e_fgpm.entity.ForecastHourlyResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForecastHourlyResultRepository extends JpaRepository<ForecastHourlyResult, Long> {
    List<ForecastHourlyResult> findByJobId(Long jobId);
}
