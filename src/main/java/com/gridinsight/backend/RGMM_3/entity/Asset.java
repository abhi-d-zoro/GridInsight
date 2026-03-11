package com.gridinsight.backend.RGMM_3.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"location", "identifier"})
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AssetType type;

    private String location;

    private String identifier; // optional but used for duplicate prevention

    private Double capacity; // MW

    private LocalDate commissionDate;

    @Enumerated(EnumType.STRING)
    private AssetStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (identifier == null || identifier.isBlank()) {
            identifier = UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
