package com.example.lucky_social_network.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "social_links")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private SocialPlatform platform;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Перечисление для поддерживаемых платформ
    @Getter
    public enum SocialPlatform {
        FACEBOOK("Facebook"),
        TWITTER("Twitter"),
        INSTAGRAM("Instagram"),
        LINKEDIN("LinkedIn"),
        GITHUB("GitHub"),
        TELEGRAM("Telegram"),
        YOUTUBE("YouTube"),
        TIKTOK("TikTok");

        private final String displayName;

        SocialPlatform(String displayName) {
            this.displayName = displayName;
        }

    }
}
