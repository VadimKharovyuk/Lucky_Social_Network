package com.example.lucky_social_network.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "groups")
public class Group implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToMany(mappedBy = "group")
    private Set<GroupMembership> memberships = new HashSet<>();

    // Вспомогательные методы
    public boolean isOwner(User user) {
        return memberships.stream()
                .anyMatch(m -> m.getUser().equals(user) &&
                        m.getRole() == GroupMembership.GroupRole.OWNER);
    }

    public boolean isAdmin(User user) {
        return memberships.stream()
                .anyMatch(m -> m.getUser().equals(user) &&
                        (m.getRole() == GroupMembership.GroupRole.ADMIN ||
                                m.getRole() == GroupMembership.GroupRole.OWNER));
    }

    public boolean isModerator(User user) {
        return memberships.stream()
                .anyMatch(m -> m.getUser().equals(user) &&
                        (m.getRole() == GroupMembership.GroupRole.MODERATOR ||
                                m.getRole() == GroupMembership.GroupRole.ADMIN ||
                                m.getRole() == GroupMembership.GroupRole.OWNER));
    }

    public boolean isMember(User user) {
        return memberships.stream()
                .anyMatch(m -> m.getUser().equals(user));
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VisibilityType visibility = VisibilityType.PUBLIC;


    @Getter
    public enum VisibilityType {
        PUBLIC("Открытая группа", "Контент доступен всем"),
        RESTRICTED("Ограниченная группа", "Контент виден после одобрения заявки"),
        PRIVATE("Закрытая группа", "Контент скрыт до вступления");

        private final String displayName;
        private final String description;

        VisibilityType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

    }

    @JsonIgnore
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupJoinRequest> joinRequests = new HashSet<>();

    // Новый флаг, определяющий, требуется ли одобрение для вступления
    @Column(nullable = false)
    private Boolean requiresJoinApproval = false;
  

    @Column(nullable = false)
    private String name;

  
    @Column(length = 2500)
    private String description;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private LocalDateTime createdAt;
    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    @Column(nullable = false)
    private Long membersCount = 0L;
    @JsonIgnore
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupPost> posts = new HashSet<>();

    @Column(nullable = false)
    private Long postsCount = 0L;

    @Column
    private String imgurImageUrl;


    private Boolean isPrivate = false;
    // Дополнительная информация
    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "website_url")
    private String websiteUrl;

    // Расширяем типы групп
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupType type;


    public enum GroupType {
        SUBSCRIPTION("Подписка"),
        INTERACTIVE("Интерактивная"),
        EDUCATIONAL("Образовательная"),
        BUSINESS("Бизнес"),
        ENTERTAINMENT("Развлекательная"),
        COMMUNITY("Сообщество"),
        NEWS("Новостная"),
        BRAND("Бренд");

        @Getter
        private final String displayName;

        GroupType(String displayName) {
            this.displayName = displayName;
        }

        @JsonIgnore
        @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
        private List<GroupRule> rules = new ArrayList<>();
    }


    // Категории и теги
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private GroupCategory category;


    public enum GroupCategory {
        TECHNOLOGY,
        SCIENCE,
        ARTS,
        MUSIC,
        SPORTS,
        GAMING,
        EDUCATION,
        BUSINESS,
        LIFESTYLE,
        OTHER
    }
}