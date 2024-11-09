package com.example.lucky_social_network.dto;

import com.example.lucky_social_network.model.Ad;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


public record AdDTO(
        Long id,
        String title,
        String content,
        String imageUrl,
        String externalUrl,
        Long ownerId,
        Long owningGroupId,
        Set<Long> targetGroupIds,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<AdScheduleDTO> schedules,
        Ad.AdStatus status,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}


