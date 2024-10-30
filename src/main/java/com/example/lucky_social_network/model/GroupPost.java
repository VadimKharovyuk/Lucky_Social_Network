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

    @Column
    private String imgurImageUrl;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Long likesCount = 0L;

    @Column(nullable = false)
    private Long commentsCount = 0L;

    private Long repostsCount = 0L;

    // Изменили тип с boolean на Boolean
    @Column(name = "important")
    private Boolean important = false;

    // Для репостов
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_group_post_id")
    private GroupPost originalGroupPost;

    // Добавили геттер для безопасного получения группы
    public Group getGroupSafely() {
        return originalGroupPost != null ? originalGroupPost.getGroup() : group;
    }

    // Безопасные геттеры для счетчиков
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
        return important != null ? important : false;
    }

    // Методы для инкрементации счетчиков
    public void incrementRepostsCount() {
        this.repostsCount = getRepostsCount() + 1;
    }

    public void incrementLikesCount() {
        this.likesCount = getLikesCount() + 1;
    }

    public void incrementCommentsCount() {
        this.commentsCount = getCommentsCount() + 1;
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

}