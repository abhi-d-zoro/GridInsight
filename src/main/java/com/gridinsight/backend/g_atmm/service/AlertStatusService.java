package com.gridinsight.backend.g_atmm.service;

import com.gridinsight.backend.g_atmm.dto.AcknowledgeAlertDTO;
import com.gridinsight.backend.g_atmm.dto.CloseAlertDTO;
import com.gridinsight.backend.g_atmm.entity.Alert;
import com.gridinsight.backend.g_atmm.entity.AlertActivity;

import java.util.List;

public interface AlertStatusService {

    Alert acknowledge(Long id, AcknowledgeAlertDTO dto);

    Alert close(Long id, CloseAlertDTO dto);

    List<AlertActivity> getActivity(Long alertId);
}