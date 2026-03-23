package com.gridinsight.backend.ATMM_7.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "threshold_rules")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ThresholdRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String metricName;

    @Enumerated(EnumType.STRING)
    private RuleScope scope;

    private Long zoneId;
    private Long assetId;

    private Double thresholdValue;

    @Enumerated(EnumType.STRING)
    private ComparisonOperator comparison;

    private String unit;

    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}