package com.gridinsight.backend.ATMM_7.service;

import com.gridinsight.backend.ATMM_7.dto.AcknowledgeAlertDTO;
import com.gridinsight.backend.ATMM_7.dto.CloseAlertDTO;
import com.gridinsight.backend.ATMM_7.entity.Alert;
import com.gridinsight.backend.ATMM_7.entity.AlertActivity;

import java.util.List;

public interface AlertStatusService {

    Alert acknowledge(Long id, AcknowledgeAlertDTO dto);

    Alert close(Long id, CloseAlertDTO dto);

    List<AlertActivity> getActivity(Long alertId);
}