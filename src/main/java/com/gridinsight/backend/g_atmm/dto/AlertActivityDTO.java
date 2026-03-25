package com.gridinsight.backend.g_atmm.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AlertActivityDTO {

    private Long id;
    private Long alertId;
    private String action;
    private String note;
    private String userId;
    private LocalDateTime timestamp;
}