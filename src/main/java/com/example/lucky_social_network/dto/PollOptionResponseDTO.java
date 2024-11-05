package com.example.lucky_social_network.dto;

import lombok.Data;

import java.util.List;

@Data
public class PollOptionResponseDTO {
    private Long id;
    private String text;
    private Long votesCount;
    private Double percentage;
    private List<UserShortDTO> voters;
    private boolean selectedByCurrentUser;


}

