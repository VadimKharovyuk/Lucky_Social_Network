package com.example.lucky_social_network.dto;

import com.example.lucky_social_network.model.Poll;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PollUpdateDTO {
    @Size(min = 5, max = 255, message = "Question must be between 5 and 255 characters")
    private String question;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Future(message = "End date must be in the future")
    private LocalDateTime endsAt;

    private Poll.PollStatus status;

    @PositiveOrZero(message = "Minimum votes to show must be non-negative")
    private Integer minimumVotesToShow;
}
