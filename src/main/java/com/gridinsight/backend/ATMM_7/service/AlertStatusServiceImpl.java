package com.gridinsight.backend.ATMM_7.service;

import com.gridinsight.backend.ATMM_7.dto.AcknowledgeAlertDTO;
import com.gridinsight.backend.ATMM_7.dto.CloseAlertDTO;
import com.gridinsight.backend.ATMM_7.entity.*;
import com.gridinsight.backend.ATMM_7.repository.AlertActivityRepository;
import com.gridinsight.backend.ATMM_7.repository.AlertRepository;
import com.gridinsight.backend.z_common.audit.AuditLogService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AlertStatusServiceImpl implements AlertStatusService {

    private final AlertRepository alerts;
    private final AlertActivityRepository activity;
    private final AuditLogService audit;

    @Override
    public Alert acknowledge(Long id, AcknowledgeAlertDTO dto) {

        Alert alert = alerts.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alert not found"));

        if (alert.getStatus() != AlertStatus.OPEN)
            throw new IllegalStateException("Only OPEN alerts can be acknowledged");

        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setAcknowledgedBy(dto.getUserId());

        alerts.save(alert);

        saveActivity(alert.getId(), "ACKNOWLEDGED", "", dto.getUserId());

        audit.logAction(
                "ALERT_ACKNOWLEDGED",
                null,
                alert.getId(),
                "Alert",
                Map.of("alertId", alert.getId(), "by", dto.getUserId())
        );

        return alert;
    }

    @Override
    public Alert close(Long id, CloseAlertDTO dto) {

        Alert alert = alerts.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alert not found"));

        if (alert.getStatus() != AlertStatus.ACKNOWLEDGED)
            throw new IllegalStateException("Only ACKNOWLEDGED alerts can be closed");

        alert.setStatus(AlertStatus.CLOSED);
        alert.setClosedAt(LocalDateTime.now());
        alert.setResolutionNote(dto.getResolutionNote());

        alerts.save(alert);

        saveActivity(alert.getId(), "CLOSED", dto.getResolutionNote(), dto.getUserId());

        audit.logAction(
                "ALERT_CLOSED",
                null,
                alert.getId(),
                "Alert",
                Map.of("alertId", alert.getId(), "note", dto.getResolutionNote())
        );

        return alert;
    }

    private void saveActivity(Long alertId, String action, String note, String userId) {
        AlertActivity a = AlertActivity.builder()
                .alertId(alertId)
                .action(action)
                .note(note)
                .userId(userId)
                .build();
        activity.save(a);
    }

    @Override
    public List<AlertActivity> getActivity(Long alertId) {
        return activity.findByAlertIdOrderByTimestampAsc(alertId);
    }
}