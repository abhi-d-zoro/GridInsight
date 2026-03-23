package com.gridinsight.backend.ATMM_7.service;

import com.gridinsight.backend.ATMM_7.dto.ThresholdRuleRequestDTO;
import com.gridinsight.backend.ATMM_7.entity.*;
import java.util.List;

public interface ThresholdRuleService {

    ThresholdRule create(ThresholdRuleRequestDTO request);

    ThresholdRule update(Long id, ThresholdRuleRequestDTO request);

    void delete(Long id);

    ThresholdRule getById(Long id);

    List<ThresholdRule> getAll();
}