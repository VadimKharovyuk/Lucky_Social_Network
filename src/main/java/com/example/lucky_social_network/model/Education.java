package com.example.lucky_social_network.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "education")
@Data
@NoArgsConstructor
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "institution_name", nullable = false)
    private String institutionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_type", nullable = false)
    private EducationType educationType;

    @Column(name = "faculty")
    private String faculty;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "degree")
    private String degree;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current")
    private boolean current;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "achievements", length = 1000)
    private String achievements;

    @Column(name = "location")
    private String location;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum EducationType {
        SCHOOL("Школа"),
        COLLEGE("Колледж"),
        UNIVERSITY("Университет"),
        ACADEMY("Академия"),
        INSTITUTE("Институт"),
        COURSES("Курсы"),
        TRAINING("Тренинг"),
        CERTIFICATION("Сертификация"),
        OTHER("Другое");

        private final String displayName;

        EducationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
