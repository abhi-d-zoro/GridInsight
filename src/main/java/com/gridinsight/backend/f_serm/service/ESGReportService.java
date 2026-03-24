package com.gridinsight.backend.f_serm.service;

import com.gridinsight.backend.f_serm.entity.ESGReport;
import com.gridinsight.backend.f_serm.repository.ESGReportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ESGReportService {

    private final ESGReportRepository repository;

    public ESGReportService(ESGReportRepository repository) {
        this.repository = repository;
    }

    public ESGReport saveReport(ESGReport report) {
        report.setGeneratedDate(LocalDate.now());
        report.setStatus("Draft");
        return repository.save(report);
    }

    public List<ESGReport> getAllReports() {
        return repository.findAll();
    }

    public String exportReportAsCSV(Long id) {
        ESGReport report = repository.findById(id).orElseThrow();
        return "ReportId,Period,Standard,Status,GeneratedDate\n" +
                report.getReportId() + "," +
                report.getPeriod() + "," +
                report.getReportingStandard() + "," +
                report.getStatus() + "," +
                report.getGeneratedDate();
    }

    // 🔹 Add this method to fix the error
    public String exportReportAsPDF(Long id) {
        ESGReport report = repository.findById(id).orElseThrow();
        // For now, return a placeholder string. Later you can integrate a PDF library like iText or Apache PDFBox.
        return "[PDF Placeholder] ESG Report " + report.getReportId() +
                " (" + report.getReportingStandard() + ", " + report.getPeriod() + ")";
    }
}
