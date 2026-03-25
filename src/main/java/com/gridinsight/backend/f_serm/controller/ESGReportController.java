package com.gridinsight.backend.f_serm.controller;

import com.gridinsight.backend.f_serm.dto.ESGReportDTO;
import com.gridinsight.backend.f_serm.entity.ESGReport;
import com.gridinsight.backend.f_serm.service.ESGReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ESGReportController {

    private final ESGReportService service;

    // ✅ Only ADMIN or ESG_ANALYST can create ESG reports
    @PreAuthorize("hasAnyRole('ADMIN','ESG_ANALYST')")
    @PostMapping
    public ESGReportDTO createReport(@RequestBody ESGReport report) {
        return service.saveReport(report);
    }

    // ✅ Only ADMIN or ESG_ANALYST can view all reports
    @PreAuthorize("hasAnyRole('ADMIN','ESG_ANALYST')")
    @GetMapping
    public List<ESGReportDTO> getReports() {
        return service.getAllReports();
    }

    // ✅ CSV export does NOT return DTO (it returns file content as String)
    @PreAuthorize("hasAnyRole('ADMIN','ESG_ANALYST')")
    @GetMapping("/{id}/export/csv")
    public String exportReportCSV(@PathVariable Long id) {
        return service.exportReportAsCSV(id);
    }

    // ✅ PDF export also returns placeholder string (NOT a DTO)
    @PreAuthorize("hasAnyRole('ADMIN','ESG_ANALYST')")
    @GetMapping("/{id}/export/pdf")
    public String exportReportPDF(@PathVariable Long id) {
        return service.exportReportAsPDF(id);
    }
}