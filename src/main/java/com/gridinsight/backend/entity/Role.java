// com.gridinsight.backend.entity.Role.java
package com.gridinsight.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "roles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Role {

    public enum RoleName { GRID_ANALYST, ASSET_MANAGER, PLANNER, ESG, ADMIN }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)                  // <-- forces VARCHAR mapping on Hibernate 6
    @Column(nullable = false, unique = true, length = 50)
    private RoleName name;
}