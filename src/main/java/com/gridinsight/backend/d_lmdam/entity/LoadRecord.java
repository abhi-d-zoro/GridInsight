package com.gridinsight.backend.d_lmdam.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "load_records")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class LoadRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "load_id")
    private Long loadId;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId; // keep as FK-id (numeric) to your zone table

    @Column(name = "ts_utc", nullable = false)
    private Instant timestamp;

    @Column(name = "demand_mw", nullable = false)
    private double demandMW;

    @Enumerated(EnumType.STRING)
    @Column(name = "demand_type", nullable = false, length = 16)
    private DemandType demandType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}