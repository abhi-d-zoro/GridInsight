package com.gridinsight.backend.f_serm.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class ESGReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    private String reportingStandard; // GRI/CDP/Internal
    private String period; // YYYY-MM
    private LocalDate generatedDate;
    private String status; // Draft/Published

    // Getters and Setters
    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public String getReportingStandard() { return reportingStandard; }
    public void setReportingStandard(String reportingStandard) { this.reportingStandard = reportingStandard; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public LocalDate getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
