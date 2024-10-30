package com.example.lucky_social_network.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "group_posts")
public class GroupPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "imgur_image_url")
    private String imgurImageUrl;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "likes_count", nullable = false)
    private Long likesCount = 0L;

    @Column(name = "comments_count", nullable = false)
    private Long commentsCount = 0L;

    @Column(name = "reposts_count")
    private Long repostsCount = 0L;

    @Column(name = "important")
    private Boolean important = false;

    @Column(name = "pinned")
    private Boolean pinned = false;

    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_group_post_id")
    private GroupPost originalGroupPost;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        if (pinned && pinnedAt == null) {
            pinnedAt = LocalDateTime.now();
        }
    }

    public Group getGroupSafely() {
        return originalGroupPost != null ? originalGroupPost.getGroup() : group;
    }

    public Long getRepostsCount() {
        return repostsCount != null ? repostsCount : 0L;
    }

    public Long getLikesCount() {
        return likesCount != null ? likesCount : 0L;
    }

    public Long getCommentsCount() {
        return commentsCount != null ? commentsCount : 0L;
    }

    public Boolean isImportant() {
        return important != null && important;
    }

    public Boolean isPinned() {
        return pinned != null && pinned;
    }

    public void incrementRepostsCount() {
        this.repostsCount = getRepostsCount() + 1;
    }

    public void incrementLikesCount() {
        this.likesCount = getLikesCount() + 1;
    }

    public void incrementCommentsCount() {
        this.commentsCount = getCommentsCount() + 1;
    }

    public boolean isRepost() {
        return originalGroupPost != null;
    }
}

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "group_id", nullable = false)
//    private Group group;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "author_id", nullable = false)
//    private User author;
//
//    @Column(nullable = false, length = 1000)
//    private String content;
//    @Column
//    private String imgurImageUrl;
//
//    @Column(nullable = false)
//    private LocalDateTime timestamp;
//
//    @Column(nullable = false)
//    private Long likesCount = 0L;
//
//    @Column(nullable = false)
//    private Long commentsCount = 0L;
//
//    private Long repostsCount = 0L;
//
//
//    // Опросы
//    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Poll poll;
//    // Закрепление и важность
//    @Column(name = "pinned")
//    private boolean pinned = false;
//
//    @Column(name = "pinned_at")
//    private LocalDateTime pinnedAt;
//
//    @Column(name = "important")
//    private boolean important = false;
//
//
//    public Long getRepostsCount() {
//        return repostsCount != null ? repostsCount : 0L;
//    }
//
//    public void setRepostsCount(Long repostsCount) {
//        this.repostsCount = repostsCount != null ? repostsCount : 0L;
//    }
//
//    public void incrementRepostsCount() {
//        this.repostsCount = (this.repostsCount != null ? this.repostsCount : 0L) + 1;
//    }

