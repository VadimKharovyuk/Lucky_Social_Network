package com.example.lucky_social_network.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateWorkExperienceDTO {

    private Long id;
    @NotBlank(message = "Название компании обязательно")
    private String companyName;

    @NotBlank(message = "Должность обязательна")
    private String position;

    @NotNull(message = "Дата начала обязательна")
    private LocalDate startDate;

    private LocalDate endDate;
    private boolean current;
    private String description;
    private String location;
    private String industry;
    private String responsibilities;
}
