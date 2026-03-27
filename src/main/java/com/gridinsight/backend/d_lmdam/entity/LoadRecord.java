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
    private String zoneId;    // ✅ String

    @Column(name = "ts_utc", nullable = false)
    private Instant timestamp;

    @Column(name = "demand_mw", nullable = false)
    private double demandMW;

    @Enumerated(EnumType.STRING)
    @Column(name = "demand_type", nullable = false)
    private DemandType demandType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }
}