package com.gridinsight.backend.IAM_1.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gridinsight.backend.IAM_1.dto.AuditLogResponse;
import com.gridinsight.backend.IAM_1.entity.AuditLog;
import com.gridinsight.backend.IAM_1.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepo;
    private final ObjectMapper objectMapper;

//    public AuditLogService(AuditLogRepository auditLogRepo, ObjectMapper objectMapper) {
//        this.auditLogRepo = auditLogRepo;
//        this.objectMapper = objectMapper;
//    }

    @Transactional
    public void logUserCreated(Long actorUserId, Long targetUserId, Map<String, Object> details) {
        createAuditLog("USER_CREATED", actorUserId, targetUserId, "User", details, null);
    }

    @Transactional
    public void logUserUpdated(Long actorUserId, Long targetUserId, Map<String, Object> changedFields) {
        createAuditLog("USER_UPDATED", actorUserId, targetUserId, "User", null, changedFields);
    }

    @Transactional
    public void logUserDeleted(Long actorUserId, Long targetUserId, Map<String, Object> details) {
        createAuditLog("USER_DELETED", actorUserId, targetUserId, "User", details, null);
    }

    @Transactional
    public void logAction(String action, Long actorUserId, Long targetUserId, String resource, Map<String, Object> metadata) {
        createAuditLog(action, actorUserId, targetUserId, resource, metadata, null);
    }

    private void createAuditLog(String action, Long actorUserId, Long targetUserId, String resource,
                                 Map<String, Object> metadata, Map<String, Object> changedFields) {
        try {
            String ipAddress = getClientIpAddress();
            String correlationId = getOrCreateCorrelationId();

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .actorUserId(actorUserId)
                    .targetUserId(targetUserId)
                    .resource(resource)
                    .timestamp(Instant.now())
                    .metadata(metadata != null ? objectMapper.writeValueAsString(metadata) : null)
                    .changedFields(changedFields != null ? objectMapper.writeValueAsString(changedFields) : null)
                    .ipAddress(ipAddress)
                    .correlationId(correlationId)
                    .build();

            auditLogRepo.save(auditLog);
            log.info("Audit log created: action={}, actor={}, target={}, ip={}, correlationId={}",
                    action, actorUserId, targetUserId, ipAddress, correlationId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit metadata", e);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    public List<AuditLogResponse> getAuditLogs(Long userId, String action, String resource,
                                                Instant fromDate, Instant toDate) {
        List<AuditLog> logs = auditLogRepo.findByFilters(userId, action, resource, fromDate, toDate);

        return logs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public String exportAuditLogsCsv(Long userId, String action, String resource,
                                     Instant fromDate, Instant toDate) {
        List<AuditLog> logs = auditLogRepo.findByFilters(userId, action, resource, fromDate, toDate);

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Actor User ID,Target User ID,Action,Resource,Timestamp,Correlation ID,IP Address,Changed Fields,Metadata\n");

        for (AuditLog log : logs) {
            csv.append(log.getId()).append(",");
            csv.append(log.getActorUserId() != null ? log.getActorUserId() : "").append(",");
            csv.append(log.getTargetUserId() != null ? log.getTargetUserId() : "").append(",");
            csv.append(escapeCsv(log.getAction())).append(",");
            csv.append(escapeCsv(log.getResource())).append(",");
            csv.append(log.getTimestamp().toString()).append(",");
            csv.append(escapeCsv(log.getCorrelationId())).append(",");
            csv.append(escapeCsv(log.getIpAddress())).append(",");
            csv.append(escapeCsv(log.getChangedFields())).append(",");
            csv.append(escapeCsv(log.getMetadata())).append("\n");
        }

        return csv.toString();
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getActorUserId())
                .userEmail(null) // Will be populated via join if needed
                .action(log.getAction())
                .resource(log.getResource())
                .timestamp(log.getTimestamp())
                .metadata(log.getMetadata())
                .correlationId(log.getCorrelationId())
                .ipAddress(log.getIpAddress())
                .build();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String getClientIpAddress() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                return "unknown";
            }

            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }

            return request.getRemoteAddr();
        } catch (Exception e) {
            log.warn("Failed to get client IP address", e);
            return "unknown";
        }
    }

    private String getOrCreateCorrelationId() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                String correlationId = request.getHeader("X-Correlation-ID");
                if (correlationId != null && !correlationId.isEmpty()) {
                    return correlationId;
                }

                // Check if already set as request attribute
                Object existingId = request.getAttribute("correlationId");
                if (existingId != null) {
                    return existingId.toString();
                }

                // Generate new one and store it
                String newId = UUID.randomUUID().toString();
                request.setAttribute("correlationId", newId);
                return newId;
            }
        } catch (Exception e) {
            log.warn("Failed to get/create correlation ID", e);
        }
        return UUID.randomUUID().toString();
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
