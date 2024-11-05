package com.example.lucky_social_network.dto;

import lombok.Data;

import java.util.List;


@Data
public class PollVoteDTO {
    // ID опроса
    private Long pollId;

    // ID выбранных вариантов (один или несколько)
    private List<Long> optionIds;

    // IP адрес (заполняется на сервере)
    private String ipAddress;
}
