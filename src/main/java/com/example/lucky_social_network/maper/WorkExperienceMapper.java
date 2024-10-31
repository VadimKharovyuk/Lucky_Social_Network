package com.example.lucky_social_network.maper;

import com.example.lucky_social_network.dto.CreateWorkExperienceDTO;
import com.example.lucky_social_network.dto.WorkExperienceDTO;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.model.WorkExperience;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class WorkExperienceMapper {

    public WorkExperienceDTO toDTO(WorkExperience workExperience) {
        WorkExperienceDTO dto = new WorkExperienceDTO();
        BeanUtils.copyProperties(workExperience, dto);
        return dto;
    }

    public WorkExperience toEntity(CreateWorkExperienceDTO dto, User user) {
        WorkExperience workExperience = new WorkExperience();
        BeanUtils.copyProperties(dto, workExperience);
        workExperience.setUser(user);
        return workExperience;
    }

    public List<WorkExperienceDTO> toDTOList(List<WorkExperience> experiences) {
        return experiences.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
