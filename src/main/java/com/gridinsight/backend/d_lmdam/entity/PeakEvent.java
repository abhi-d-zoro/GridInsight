package com.gridinsight.backend.d_lmdam.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "peak_events")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PeakEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "peak_id")
    private Long peakId;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    @Column(name = "start_utc", nullable = false)
    private Instant startTime;

    @Column(name = "end_utc", nullable = false)
    private Instant endTime;

    @Column(name = "peak_mw", nullable = false)
    private double peakMW;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 16)
    private Severity severity;

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