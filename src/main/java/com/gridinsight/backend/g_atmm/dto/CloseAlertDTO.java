package com.gridinsight.backend.g_atmm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CloseAlertDTO {
    @NotBlank
    private String resolutionNote;
}
