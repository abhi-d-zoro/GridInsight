package com.gridinsight.backend.g_atmm.service;

import com.gridinsight.backend.g_atmm.dto.AcknowledgeAlertDTO;
import com.gridinsight.backend.g_atmm.dto.AlertActivityDTO;
import com.gridinsight.backend.g_atmm.dto.AlertDTO;
import com.gridinsight.backend.g_atmm.dto.CloseAlertDTO;
import com.gridinsight.backend.g_atmm.entity.Alert;
import com.gridinsight.backend.g_atmm.entity.AlertActivity;
import com.gridinsight.backend.g_atmm.entity.AlertStatus;
import com.gridinsight.backend.g_atmm.repository.AlertActivityRepository;
import com.gridinsight.backend.g_atmm.repository.AlertRepository;
import com.gridinsight.backend.z_common.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AlertStatusService {

    private final AlertRepository alerts;
    private final AlertActivityRepository activity;
    private final AuditLogService audit;

    // ✅ Acknowledge alert → returns AlertDTO
    public AlertDTO acknowledge(Long id, AcknowledgeAlertDTO dto) {

        Alert alert = alerts.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alert not found"));

        if (alert.getStatus() != AlertStatus.OPEN)
            throw new IllegalStateException("Only OPEN alerts can be acknowledged");

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setAcknowledgedBy(currentUser);

        alerts.save(alert);

        saveActivity(alert.getId(), "ACKNOWLEDGED", "", currentUser);

        audit.logAction(
                "ALERT_ACKNOWLEDGED",
                null,
                alert.getId(),
                "Alert",
                Map.of("alertId", alert.getId(), "by", currentUser)
        );

        return toDTO(alert);
    }

    // ✅ Close alert → returns AlertDTO
    public AlertDTO close(Long id, CloseAlertDTO dto) {

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        Alert alert = alerts.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alert not found"));

        if (alert.getStatus() != AlertStatus.ACKNOWLEDGED)
            throw new IllegalStateException("Only ACKNOWLEDGED alerts can be closed");

        alert.setStatus(AlertStatus.CLOSED);
        alert.setClosedAt(LocalDateTime.now());
        alert.setResolutionNote(dto.getResolutionNote());

        alerts.save(alert);

        saveActivity(alert.getId(), "CLOSED", dto.getResolutionNote(), currentUser);

        audit.logAction(
                "ALERT_CLOSED",
                null,
                alert.getId(),
                "Alert",
                Map.of("alertId", alert.getId(), "note", dto.getResolutionNote())
        );

        return toDTO(alert);
    }

    // ✅ Get alert activity → returns List<AlertActivityDTO>
    public List<AlertActivityDTO> getActivity(Long alertId) {
        return activity.findByAlertIdOrderByTimestampAsc(alertId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ✅ Save activity (internal helper)
    private void saveActivity(Long alertId, String action, String note, String userId) {
        AlertActivity a = AlertActivity.builder()
                .alertId(alertId)
                .action(action)
                .note(note)
                .userId(userId)
                .build();
        activity.save(a);
    }

    // ✅ Mappers

    private AlertDTO toDTO(Alert alert) {
        return AlertDTO.builder()
                .id(alert.getId())
                .ruleId(alert.getRuleId())
                .zoneId(alert.getZoneId())
                .assetId(alert.getAssetId())
                .metricName(alert.getMetricName())
                .actualValue(alert.getActualValue())
                .thresholdValue(alert.getThresholdValue())
                .comparison(alert.getComparison())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .message(alert.getMessage())
                .correlationId(alert.getCorrelationId())
                .acknowledgedAt(alert.getAcknowledgedAt())
                .acknowledgedBy(alert.getAcknowledgedBy())
                .closedAt(alert.getClosedAt())
                .resolutionNote(alert.getResolutionNote())
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .build();
    }

    private AlertActivityDTO toDTO(AlertActivity a) {
        return AlertActivityDTO.builder()
                .id(a.getId())
                .alertId(a.getAlertId())
                .action(a.getAction())
                .note(a.getNote())
                .userId(a.getUserId())
                .timestamp(a.getTimestamp())
                .build();
    }
}