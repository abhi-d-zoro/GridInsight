package com.gridinsight.backend.ATMM_7.service;

import com.gridinsight.backend.ATMM_7.dto.CheckValueRequestDTO;
import com.gridinsight.backend.ATMM_7.entity.Alert;
import java.util.List;

public interface AlertService {
    List<Alert> evaluate(CheckValueRequestDTO dto);
    Alert getById(Long id);
    List<Alert> getAll();
}