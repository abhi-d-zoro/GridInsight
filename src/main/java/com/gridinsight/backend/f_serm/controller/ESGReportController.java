package com.gridinsight.backend.f_serm.controller;

import com.gridinsight.backend.f_serm.entity.ESGReport;
import com.gridinsight.backend.f_serm.service.ESGReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ESGReportController {

    private final ESGReportService service;

    public ESGReportController(ESGReportService service) {
        this.service = service;
    }

    /**
     * Create a new ESG report.
     * Example: POST /api/reports
     * Body:
     * {
     *   "reportingStandard": "GRI",
     *   "period": "2026-01"
     * }
     */
    @PostMapping
    public ESGReport createReport(@RequestBody ESGReport report) {
        return service.saveReport(report);
    }

    /**
     * Retrieve all ESG reports.
     * Example: GET /api/reports
     */
    @GetMapping
    public List<ESGReport> getReports() {
        return service.getAllReports();
    }

    /**
     * Export a report as CSV.
     * Example: GET /api/reports/1/export/csv
     */
    @GetMapping("/{id}/export/csv")
    public String exportReportCSV(@PathVariable Long id) {
        return service.exportReportAsCSV(id);
    }

    /**
     * Export a report as PDF (placeholder).
     * Example: GET /api/reports/1/export/pdf
     */
    @GetMapping("/{id}/export/pdf")
    public String exportReportPDF(@PathVariable Long id) {
        return service.exportReportAsPDF(id);
    }
}
