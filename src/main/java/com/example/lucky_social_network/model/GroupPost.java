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

    public Long getRepostsCount() {
        return repostsCount != null ? repostsCount : 0L;
    }

    public void setRepostsCount(Long repostsCount) {
        this.repostsCount = repostsCount != null ? repostsCount : 0L;
    }

    public void incrementRepostsCount() {
        this.repostsCount = (this.repostsCount != null ? this.repostsCount : 0L) + 1;
    }

    // Можно добавить связи для комментариев и лайков, если они нужны
    // @OneToMany(mappedBy = "groupPost", cascade = CascadeType.ALL, orphanRemoval = true)
    // private Set<GroupPostComment> comments = new HashSet<>();

    // @OneToMany(mappedBy = "groupPost", cascade = CascadeType.ALL, orphanRemoval = true)
    // private Set<GroupPostLike> likes = new HashSet<>();
}