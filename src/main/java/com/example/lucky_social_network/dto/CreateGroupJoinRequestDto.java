package com.example.lucky_social_network.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateGroupJoinRequestDto {
    @NotNull
    private Long groupId;

    private String message;
}
