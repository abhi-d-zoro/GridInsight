package com.gridinsight.backend.a_iam.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Store as uppercase strings: ADMIN, ESG, ASSET_MANAGER, PLANNER, etc.
     * Spring Security convention is to prefix with "ROLE_" (e.g., ROLE_ADMIN).
     */
    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;

    // Convenience method for integration with Spring Security
    public String getAuthority() {
        return "ROLE_" + name.toUpperCase();
    }
}
