package com.example.lucky_social_network.service;

import com.example.lucky_social_network.dto.CreateWorkExperienceDTO;
import com.example.lucky_social_network.dto.WorkExperienceDTO;
import com.example.lucky_social_network.exception.WorkExperienceNotFoundException;
import com.example.lucky_social_network.maper.WorkExperienceMapper;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.model.WorkExperience;
import com.example.lucky_social_network.repository.WorkExperienceRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class WorkExperienceService {

    private final WorkExperienceRepository workExperienceRepository;
    private final WorkExperienceMapper mapper;
    private final UserService userService;

    @Transactional
    public WorkExperienceDTO addWorkExperience(Long userId, CreateWorkExperienceDTO dto) {
        User user = userService.getUserById(userId);
        validateWorkExperience(dto);

        if (dto.isCurrent()) {
            deactivateCurrentWorkExperience(userId);
        }

        WorkExperience workExperience = mapper.toEntity(dto, user);
        WorkExperience saved = workExperienceRepository.save(workExperience);

        log.info("Added new work experience for user {}: {}", userId, saved.getCompanyName());
        return mapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public WorkExperienceDTO getWorkExperienceById(Long userId, Long id) {
        WorkExperience workExperience = workExperienceRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new WorkExperienceNotFoundException(
                        String.format("Work experience with id %d not found for user %d", id, userId)
                ));

        return mapper.toDTO(workExperience);
    }

    @Transactional(readOnly = true)
    public List<WorkExperienceDTO> getAllWorkExperience(Long userId) {
        return mapper.toDTOList(
                workExperienceRepository.findByUserIdOrderByStartDateDesc(userId)
        );
    }

    @Transactional
    public WorkExperienceDTO updateWorkExperience(Long userId, Long id, CreateWorkExperienceDTO dto) {
        WorkExperience workExperience = workExperienceRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new WorkExperienceNotFoundException(id));

        validateWorkExperience(dto);

        if (dto.isCurrent() && !workExperience.isCurrent()) {
            deactivateCurrentWorkExperience(userId);
        }

        BeanUtils.copyProperties(dto, workExperience);
        WorkExperience saved = workExperienceRepository.save(workExperience);

        log.info("Updated work experience {} for user {}", id, userId);
        return mapper.toDTO(saved);
    }

    @Transactional
    public void deleteWorkExperience(Long userId, Long id) {
        WorkExperience workExperience = workExperienceRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new WorkExperienceNotFoundException(id));

        workExperienceRepository.delete(workExperience);
        log.info("Deleted work experience {} for user {}", id, userId);
    }

    private void validateWorkExperience(CreateWorkExperienceDTO dto) {
        if (dto.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required");
        }

        if (!dto.isCurrent() && dto.getEndDate() == null) {
            throw new IllegalArgumentException("End date is required for non-current work experience");
        }

        if (dto.getEndDate() != null && dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }

    private void deactivateCurrentWorkExperience(Long userId) {
        workExperienceRepository.findByUserIdOrderByStartDateDesc(userId).stream()
                .filter(WorkExperience::isCurrent)
                .forEach(work -> {
                    work.setCurrent(false);
                    work.setEndDate(LocalDate.now());
                    workExperienceRepository.save(work);
                });
    }
}

