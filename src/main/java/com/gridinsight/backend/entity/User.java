package com.gridinsight.backend.entity;

import jakarta.persistence.*;                // <-- make sure this exists
import lombok.*;

// If you used the VARCHAR-for-enum approach earlier:
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;

@Entity                                   // <-- will resolve once jakarta import is present
@Table(name = "users")
@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private String name;

    @Column(nullable=false, unique=true)
    private String email;

    private String phone;

    @Column(name="password_hash", nullable=false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)       // keep if you chose the VARCHAR mapping for enums
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}