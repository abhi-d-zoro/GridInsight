package com.gridinsight.backend.g_atmm.service;

import com.gridinsight.backend.g_atmm.dto.CheckValueRequestDTO;
import com.gridinsight.backend.g_atmm.entity.Alert;
import java.util.List;

public interface AlertService {
    List<Alert> evaluate(CheckValueRequestDTO dto);
    Alert getById(Long id);
    List<Alert> getAll();
}