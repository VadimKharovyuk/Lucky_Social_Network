package com.example.lucky_social_network.dto;

import com.example.lucky_social_network.model.Education;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EducationDTO {
    private Long id;
    private String institutionName;
    private Education.EducationType educationType;
    private String faculty;
    private String specialization;
    private String degree;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean current;
    private String description;
    private String achievements;
    private String location;

}
