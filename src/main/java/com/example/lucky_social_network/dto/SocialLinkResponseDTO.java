package com.example.lucky_social_network.dto;

import com.example.lucky_social_network.model.SocialLink;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class SocialLinkResponseDTO {
    private Long id;
    private Long userId;
    private SocialLink.SocialPlatform platform;
    private String url;
    private String displayName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}