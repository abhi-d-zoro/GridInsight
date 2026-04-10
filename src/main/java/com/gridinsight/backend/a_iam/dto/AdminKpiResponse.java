package com.gridinsight.backend.a_iam.dto;

public record AdminKpiResponse(
        long totalUsers,
        long totalGridZones,
        long activeAlerts
) {}