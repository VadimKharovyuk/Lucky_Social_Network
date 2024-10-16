package com.example.lucky_social_network.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private AdminRole role;

    private String twoFactorSecret;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    public enum AdminRole {
        JUNIOR_ADMIN,
        SENIOR_ADMIN,
        SUPER_ADMIN
    }

    public static User.Role mapAdminRoleToUserRole(AdminRole adminRole) {
        switch (adminRole) {
            case JUNIOR_ADMIN:
            case SENIOR_ADMIN:
            case SUPER_ADMIN:
                return User.Role.ADMIN;
            default:
                throw new IllegalArgumentException("Unknown admin role: " + adminRole);
        }
    }
}
