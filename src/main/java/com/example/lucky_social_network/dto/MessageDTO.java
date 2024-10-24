package com.example.lucky_social_network.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MessageDTO {
    private String type;
    private Long senderId;
    private String content;
    private String timestamp;
}
