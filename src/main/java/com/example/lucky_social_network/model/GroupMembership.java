package com.example.lucky_social_network.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_memberships")
@Getter
@Setter
@NoArgsConstructor
public class GroupMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    // Метод установки роли
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupRole role = GroupRole.MEMBER;

    @CreationTimestamp
    private LocalDateTime joinedAt;

    // Правильный метод установки времени назначения роли
    // Добавляем поле для времени назначения роли
    @Setter
    private LocalDateTime roleAssignedAt;

    public enum GroupRole {
        OWNER,
        ADMIN,
        MODERATOR,
        MEMBER
    }

}