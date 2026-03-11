package com.gridinsight.backend.RGMM_3.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record AssetRequest(
        @NotNull String type,
        @NotBlank String location,
        String identifier,
        @NotNull @Positive Double capacity,
        @NotNull LocalDate commissionDate
) {}
