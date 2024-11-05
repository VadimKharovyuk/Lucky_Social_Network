package com.example.lucky_social_network.dto;

import com.example.lucky_social_network.model.Poll;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PollResponseDTO {
    private Long id;
    private String question;
    private String description;

    // Информация о посте
    private Long postId;

    // Варианты ответов с результатами
    private List<PollOptionResponseDTO> options;

    // Настройки опроса
    private LocalDateTime endsAt;
    private boolean multipleChoice;
    private boolean anonymous;
    private Integer maxChoices;

    // Статистика
    private Long totalVotes;
    private Long uniqueVoters;

    // Статус
    private Poll.PollStatus status;

    // Флаги для текущего пользователя
    private boolean hasVoted;
    private boolean canVote;

    // Метаданные
    private LocalDateTime createdAt;
    private UserShortDTO createdBy;


}
