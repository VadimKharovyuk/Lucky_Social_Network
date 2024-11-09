package com.example.lucky_social_network.dto;

import com.example.lucky_social_network.model.Ad;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record AdUpdateRequest(
        String title,
        String content,
        String imageUrl,
        String externalUrl,
        Set<Long> targetGroupIds,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<AdScheduleDTO> schedules,
        Ad.AdStatus status
) {
}
