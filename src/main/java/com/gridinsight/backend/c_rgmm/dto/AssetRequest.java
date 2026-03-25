package com.gridinsight.backend.c_rgmm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record AssetRequest(
        @NotNull String type,
        @NotBlank String location,
        String identifier,
        @NotNull @Positive Double capacity,
        @NotNull LocalDate commissionDate
) {}
