package com.example.lucky_social_network.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkExperienceDTO {

    private Long id;
    private String companyName;
    private String position;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean current;
    private String description;
    private String location;
    private String industry;
    private String responsibilities;
}
