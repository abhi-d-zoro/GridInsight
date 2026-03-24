package com.gridinsight.backend.g_atmm.service;

import com.gridinsight.backend.g_atmm.dto.ThresholdRuleRequestDTO;
import com.gridinsight.backend.g_atmm.entity.*;
import java.util.List;

public interface ThresholdRuleService {

    ThresholdRule create(ThresholdRuleRequestDTO request);

    ThresholdRule update(Long id, ThresholdRuleRequestDTO request);

    void delete(Long id);

    ThresholdRule getById(Long id);

    List<ThresholdRule> getAll();
}