package com.gridinsight.backend.c_rgmm.service;

import com.gridinsight.backend.c_rgmm.exception.CsvValidationException;
import com.gridinsight.backend.c_rgmm.entity.GenerationRecord;
import com.gridinsight.backend.c_rgmm.repository.GenerationRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GenerationRecordService {

    private final GenerationRecordRepository repository;

    public Map<String, Object> uploadCsv(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int processed = 0, success = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new CsvValidationException("Empty CSV file");
            }

            String[] headers = headerLine.split(",");
            List<String> required = List.of("AssetID", "Timestamp", "GeneratedEnergyMWh", "AvailabilityPct");
            if (!Arrays.asList(headers).containsAll(required)) {
                throw new CsvValidationException("Missing required columns: " + required);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                processed++;
                try {
                    String[] values = line.split(",");
                    String assetId = values[0].trim();
                    LocalDateTime timestamp = LocalDateTime.parse(values[1].trim());
                    double energy = Double.parseDouble(values[2].trim());
                    double availability = Double.parseDouble(values[3].trim());

                    if (energy < 0 || availability < 0 || availability > 100) {
                        throw new CsvValidationException("Invalid values at row " + processed);
                    }

                    GenerationRecord entity = repository
                            .findByAssetIdAndTimestamp(assetId, timestamp)
                            .orElse(new GenerationRecord());

                    entity.setAssetId(assetId);
                    entity.setTimestamp(timestamp);
                    entity.setGeneratedEnergyMWh(energy);
                    entity.setAvailabilityPct(availability);

                    repository.save(entity);
                    success++;

                } catch (DateTimeParseException | NumberFormatException | CsvValidationException e) {
                    errors.add("Row " + processed + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new CsvValidationException("CSV parsing failed: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("processed", processed);
        result.put("success", success);
        result.put("failed", processed - success);
        result.put("errors", errors);
        return result;
    }
}
