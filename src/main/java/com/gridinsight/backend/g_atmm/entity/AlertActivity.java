package com.gridinsight.backend.g_atmm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alert_activity")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AlertActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long alertId;
    private String action;
    private String note;
    private String userId;

    private LocalDateTime timestamp;

    @PrePersist
    public void onCreate(){
        timestamp = LocalDateTime.now();
    }
}