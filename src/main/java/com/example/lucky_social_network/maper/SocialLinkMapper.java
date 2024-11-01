package com.example.lucky_social_network.maper;

import com.example.lucky_social_network.dto.SocialLinkCreateDTO;
import com.example.lucky_social_network.dto.SocialLinkResponseDTO;
import com.example.lucky_social_network.dto.SocialLinkUpdateDTO;
import com.example.lucky_social_network.model.SocialLink;
import org.springframework.stereotype.Component;

@Component
public class SocialLinkMapper {
    // Из CreateDTO в Entity
    public SocialLink toEntity(SocialLinkCreateDTO dto) {
        SocialLink socialLink = new SocialLink();
        socialLink.setPlatform(dto.getPlatform());
        socialLink.setUrl(dto.getUrl());
        // createdAt и updatedAt установим в сервисе
        return socialLink;
    }

    // Из Entity в ResponseDTO
    public SocialLinkResponseDTO toResponseDto(SocialLink socialLink) {
        SocialLinkResponseDTO dto = new SocialLinkResponseDTO();
        dto.setId(socialLink.getId());
        dto.setUserId(socialLink.getUser().getId());
        dto.setPlatform(socialLink.getPlatform());
        dto.setUrl(socialLink.getUrl());
        dto.setDisplayName(socialLink.getPlatform().getDisplayName());
        dto.setCreatedAt(socialLink.getCreatedAt());
        dto.setUpdatedAt(socialLink.getUpdatedAt());
        return dto;
    }

    // Обновление Entity из UpdateDTO
    public void updateEntityFromDto(SocialLinkUpdateDTO dto, SocialLink socialLink) {
        if (dto.getUrl() != null) {
            socialLink.setUrl(dto.getUrl());
        }
        if (dto.getPlatform() != null) {
            socialLink.setPlatform(dto.getPlatform());
        }
        // updatedAt установим в сервисе
    }
}