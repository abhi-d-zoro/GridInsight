package com.gridinsight.backend.c_rgmm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "generation_records",
        uniqueConstraints = @UniqueConstraint(columnNames = {"assetId", "timestamp"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String assetId;

    private LocalDateTime timestamp;

    private Double generatedEnergyMWh;

    private Double availabilityPct;
}
