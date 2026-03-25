package com.gridinsight.backend.f_serm.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ESGReportDTO {

    private Long reportId;
    private String reportingStandard; // GRI/CDP/Internal
    private String period;            // YYYY-MM
    private LocalDate generatedDate;
    private String status;            // Draft/Published
}