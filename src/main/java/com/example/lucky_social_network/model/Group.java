package com.example.lucky_social_network.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

  
    @Column(length = 2500)
    private String description;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    @Column(nullable = false)
    private Long membersCount = 0L;

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