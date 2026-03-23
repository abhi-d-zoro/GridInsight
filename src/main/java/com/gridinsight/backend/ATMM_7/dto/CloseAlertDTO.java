package com.gridinsight.backend.ATMM_7.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CloseAlertDTO {

    @NotBlank
    private String resolutionNote;

    @NotBlank
    private String userId;
}
