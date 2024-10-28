package com.example.lucky_social_network.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "blog_posts", indexes = {
        @Index(name = "idx_blog_posts_type", columnList = "type"),
        @Index(name = "idx_blog_posts_created_at", columnList = "created_at"),
        @Index(name = "idx_blog_posts_view_count", columnList = "view_count")
})
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlogType type;

    @ElementCollection
    @CollectionTable(name = "blog_post_images",
            joinColumns = @JoinColumn(name = "blog_post_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean published = false; // По умолчанию false

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        viewCount = 0L;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
    }


    public enum BlogType {
        PRODUCT("Продукт"),
        EVENT("События"),
        API("API"),
        DEVELOPMENT("Разработка"),
        SECURITY("Безопасность");

        private final String displayName;

        BlogType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}