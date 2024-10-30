package com.example.lucky_social_network.maper;

import com.example.lucky_social_network.dto.CreateEducationDTO;
import com.example.lucky_social_network.dto.EducationDTO;
import com.example.lucky_social_network.model.Education;
import com.example.lucky_social_network.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EducationMapper {

    public EducationDTO toDTO(Education education) {
        EducationDTO dto = new EducationDTO();
        BeanUtils.copyProperties(education, dto);
        return dto;
    }

    public Education toEntity(CreateEducationDTO dto, User user) {
        Education education = new Education();
        BeanUtils.copyProperties(dto, education);
        education.setUser(user);
        education.setCreatedAt(LocalDateTime.now());
        return education;
    }
}
