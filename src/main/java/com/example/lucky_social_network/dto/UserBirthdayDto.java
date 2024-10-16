package com.example.lucky_social_network.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Value;

import java.time.LocalDate;


@Value
public class UserBirthdayDto {
    Long userId;
    String username;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate birthday;
    long daysUntil;
}