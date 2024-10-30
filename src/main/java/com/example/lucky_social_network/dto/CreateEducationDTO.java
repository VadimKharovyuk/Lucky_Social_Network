package com.example.lucky_social_network.dto;

import com.example.lucky_social_network.model.Education;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateEducationDTO {

    private Long id;
    @NotBlank(message = "Institution name is required")
    private String institutionName;

    @NotNull(message = "Education type is required")
    private Education.EducationType educationType;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;
    private boolean current;
    private String faculty;
    private String specialization;
    private String degree;
    private String description;
    private String achievements;
    private String location;


}
