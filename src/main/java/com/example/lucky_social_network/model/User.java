package com.example.lucky_social_network.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;
    private String phone;
    private String bio;

    // Используем bytea для бинарных данных
    @Lob
    private byte[] avatar;

    private LocalDate createdAt;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    private Boolean emailVerified;
    private Integer followersCount;
    private Integer friendsCount;

    private Boolean isPrivate;
    private LocalDateTime lastLogin;
    private String location;

    public User(Long id) {
        this.id = id;
    }


    public enum Gender {
        MALE,
        FEMALE
    }
}