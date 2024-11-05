package com.example.lucky_social_network.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class PollCreateDTO {
    @NotNull(message = "Group ID is required")
    private Long groupId;

    // Поля для поста
    private Long postId;  // ID существующего поста
    private String postContent;  // Контент для нового поста

    @NotBlank(message = "Question cannot be empty")
    @Size(min = 5, max = 255, message = "Question must be between 5 and 255 characters")
    private String question;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotEmpty(message = "Poll must have at least one option")
    @Size(min = 2, max = 10, message = "Poll must have between 2 and 10 options")
    private List<String> options = new ArrayList<>();

    @Future(message = "End date must be in the future")
    private LocalDateTime endsAt;

    private boolean multipleChoice = false;
    private boolean anonymous = false;

    @Min(value = 0, message = "Minimum votes to show must be non-negative")
    private Integer minimumVotesToShow = 0;

    @Min(value = 1, message = "Maximum choices must be at least 1")
    private Integer maxChoices = 1;

    private boolean requiresVerification = false;
    private boolean oneVotePerIp = false;

    // Валидация для поста
    @AssertTrue(message = "Cannot specify both postId and postContent")
    private boolean isPostValid() {
        return !(postId != null && postContent != null);
    }

    // Существующий метод валидации
    @AssertTrue(message = "Max choices must be specified and valid for multiple choice polls")
    private boolean isMaxChoicesValid() {
        if (multipleChoice) {
            return maxChoices != null && maxChoices > 1 && maxChoices <= options.size();
        }
        return true;
    }

}
