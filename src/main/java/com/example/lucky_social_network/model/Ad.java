package com.example.lucky_social_network.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "advertisements")
public class Ad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    private String imageUrl;

    private String externalUrl;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group owningGroup;

    @ManyToMany
    @JoinTable(
            name = "ad_target_groups",
            joinColumns = @JoinColumn(name = "ad_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Set<Group> targetGroups = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @OneToMany(mappedBy = "ad", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AdSchedule> schedules = new HashSet<>();

    // Метод-помощник для добавления расписания
    public void addSchedule(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        AdSchedule schedule = new AdSchedule(this, dayOfWeek, startTime, endTime);
        schedules.add(schedule);
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdStatus status = AdStatus.DRAFT;

    @Column(nullable = false)
    private boolean isActive = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum AdStatus {
        DRAFT, PENDING, ACTIVE, PAUSED, COMPLETED, REJECTED
    }

}