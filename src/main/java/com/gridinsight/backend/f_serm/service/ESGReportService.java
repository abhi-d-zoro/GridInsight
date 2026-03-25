package com.gridinsight.backend.f_serm.service;

import com.gridinsight.backend.f_serm.dto.ESGReportDTO;
import com.gridinsight.backend.f_serm.entity.ESGReport;
import com.gridinsight.backend.f_serm.repository.ESGReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ESGReportService {

    private final ESGReportRepository repository;

    public ESGReportDTO saveReport(ESGReport report) {
        report.setGeneratedDate(LocalDate.now());
        report.setStatus("Draft");
        ESGReport saved = repository.save(report);
        return toDTO(saved);
    }

    public List<ESGReportDTO> getAllReports() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .toList();
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

    public String exportReportAsPDF(Long id) {
        ESGReport report = repository.findById(id).orElseThrow();
        return "[PDF Placeholder] ESG Report " + report.getReportId() +
                " (" + report.getReportingStandard() + ", " + report.getPeriod() + ")";
    }

    private ESGReportDTO toDTO(ESGReport e) {
        return ESGReportDTO.builder()
                .reportId(e.getReportId())
                .period(e.getPeriod())
                .reportingStandard(e.getReportingStandard())
                .status(e.getStatus())
                .generatedDate(e.getGeneratedDate())
                .build();
    }
}