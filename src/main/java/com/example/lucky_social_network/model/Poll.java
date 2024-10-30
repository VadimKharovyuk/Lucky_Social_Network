package com.example.lucky_social_network.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "polls")
@Data
public class Poll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связь с постом
    @OneToOne
    @JoinColumn(name = "post_id")
    private GroupPost post;

    // Основная информация
    @Column(nullable = false)
    private String question;

    @Column(name = "description", length = 500)
    private String description;

    // Варианты ответов
    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<PollOption> options = new ArrayList<>();

    // Настройки опроса
    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(name = "multiple_choice")
    private boolean multipleChoice = false;

    @Column(name = "anonymous")
    private boolean anonymous = false;

    @Column(name = "max_choices")
    private Integer maxChoices; // Для multiple choice

    @Column(name = "minimum_votes_to_show")
    private Integer minimumVotesToShow; // Минимум голосов для показа результатов

    // Статус опроса
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PollStatus status = PollStatus.ACTIVE;

    // Статистика
    @Column(name = "total_votes")
    private Long totalVotes = 0L;

    @Column(name = "unique_voters")
    private Long uniqueVoters = 0L;

    // Защита от накрутки
    @Column(name = "requires_verification")
    private boolean requiresVerification = false;

    @Column(name = "one_vote_per_ip")
    private boolean oneVotePerIp = false;

    // История голосования
    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL)
    private List<PollVote> votes = new ArrayList<>();

    // Аудит
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @Column(name = "last_modified_by")
    private Long lastModifiedBy;

    // Перечисления
    public enum PollStatus {
        DRAFT,
        ACTIVE,
        PAUSED,
        ENDED,
        CANCELLED
    }
}

