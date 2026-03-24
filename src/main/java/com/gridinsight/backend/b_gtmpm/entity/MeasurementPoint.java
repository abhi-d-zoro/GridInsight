package com.gridinsight.backend.b_gtmpm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "measurement_points",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_zone_identifier", columnNames = {"zone_id", "identifier"})
        },
        indexes = {
                @Index(name = "idx_mp_zone_id", columnList = "zone_id"),
                @Index(name = "idx_mp_identifier", columnList = "identifier")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MeasurementPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Zone required
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mp_zone"))
    private GridZone zone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AssetType assetType;

    @Column(nullable = false, length = 128)
    private String identifier; // unique per zone

    @Column(nullable = false, length = 64)
    private String unit;       // e.g., kV, MW, A, custom strings

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PointStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}