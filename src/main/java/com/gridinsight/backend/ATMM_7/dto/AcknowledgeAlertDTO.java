package com.gridinsight.backend.ATMM_7.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AcknowledgeAlertDTO {
    @NotBlank
    private String userId;
}
