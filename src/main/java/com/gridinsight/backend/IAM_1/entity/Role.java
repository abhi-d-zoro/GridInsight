 package com.gridinsight.backend.IAM_1.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Store as simple uppercase strings: ADMIN, GRID_ANALYST, ASSET_MANAGER, PLANNER, ESG
    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;
}
