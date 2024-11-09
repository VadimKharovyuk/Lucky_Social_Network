package com.example.lucky_social_network.dto;

import com.example.lucky_social_network.model.Ad;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public record AdCreateRequest(
        String title,
        String content,
        String imageUrl,
        String externalUrl,
        Long owningGroupId,
        Set<Long> targetGroupIds,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<AdScheduleDTO> schedules,
        Ad.AdStatus status
) {
    // Конструктор с валидацией
    public AdCreateRequest {
        if (schedules == null) {
            schedules = Arrays.asList(
                    new AdScheduleDTO(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                    new AdScheduleDTO(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                    new AdScheduleDTO(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                    new AdScheduleDTO(DayOfWeek.THURSDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                    new AdScheduleDTO(DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                    new AdScheduleDTO(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                    new AdScheduleDTO(DayOfWeek.SUNDAY, LocalTime.of(9, 0), LocalTime.of(18, 0))
            );
        }
        if (targetGroupIds == null) {
            targetGroupIds = new HashSet<>();
        }
        if (status == null) {
            status = Ad.AdStatus.DRAFT;  // По умолчанию DRAFT
        }
    }

    public static AdCreateRequest createEmpty() {
        return new AdCreateRequest(
                "",                  // title
                "",                  // content
                null,               // imageUrl
                null,               // externalUrl
                null,               // owningGroupId
                new HashSet<>(),    // targetGroupIds
                LocalDateTime.now(), // startTime
                LocalDateTime.now().plusDays(7), // endTime
                Arrays.asList(      // schedules
                        new AdScheduleDTO(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                        new AdScheduleDTO(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                        new AdScheduleDTO(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                        new AdScheduleDTO(DayOfWeek.THURSDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                        new AdScheduleDTO(DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                        new AdScheduleDTO(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                        new AdScheduleDTO(DayOfWeek.SUNDAY, LocalTime.of(9, 0), LocalTime.of(18, 0))
                ),
                Ad.AdStatus.DRAFT   // status по умолчанию
        );
    }
}