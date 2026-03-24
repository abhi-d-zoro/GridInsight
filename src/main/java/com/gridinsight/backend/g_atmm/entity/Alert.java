package com.gridinsight.backend.g_atmm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ruleId;
    private Long zoneId;
    private Long assetId;

    private String metricName;
    private Double actualValue;
    private Double thresholdValue;

    @Enumerated(EnumType.STRING)
    private ComparisonOperator comparison;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    private AlertStatus status;

    private String message;
    private String correlationId;

    private LocalDateTime acknowledgedAt;
    private String acknowledgedBy;

    private LocalDateTime closedAt;
    private String resolutionNote;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = AlertStatus.OPEN;
    }

    @PreUpdate
    public void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}