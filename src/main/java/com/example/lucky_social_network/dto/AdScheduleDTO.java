package com.example.lucky_social_network.dto;

// AdScheduleDTO.java


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;


@Getter
@Setter
@NoArgsConstructor
public class AdScheduleDTO {
    @NotNull(message = "День недели обязателен")
    private DayOfWeek dayOfWeek = DayOfWeek.MONDAY;

    @NotNull(message = "Время начала обязательно")
    private LocalTime startTime = LocalTime.of(9, 0);

    @NotNull(message = "Время окончания обязательно")
    private LocalTime endTime = LocalTime.of(18, 0);

    public AdScheduleDTO(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek != null ? dayOfWeek : DayOfWeek.MONDAY;
        this.startTime = startTime != null ? startTime : LocalTime.of(9, 0);
        this.endTime = endTime != null ? endTime : LocalTime.of(18, 0);
    }
}
