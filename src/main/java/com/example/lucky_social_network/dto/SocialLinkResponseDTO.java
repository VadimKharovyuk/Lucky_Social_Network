package com.example.lucky_social_network.dto;

import com.example.lucky_social_network.model.SocialLink;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SocialLinkResponseDTO {
    private Long id;
    private Long userId;
    private SocialLink.SocialPlatform platform;
    private String url;
    private String displayName; // имя платформы
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}