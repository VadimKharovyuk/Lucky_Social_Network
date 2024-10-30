package com.example.lucky_social_network.service;

import com.example.lucky_social_network.dto.CreateEducationDTO;
import com.example.lucky_social_network.dto.EducationDTO;
import com.example.lucky_social_network.exception.EducationNotFoundException;
import com.example.lucky_social_network.maper.EducationMapper;
import com.example.lucky_social_network.model.Education;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.EducationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class EducationService {

    private final EducationMapper mapper;
    private final UserService userService;
    private final EducationRepository educationRepository;

    public EducationDTO addEducation(Long userId, CreateEducationDTO dto) {
        User user = userService.getUserById(userId);
        Education education = mapper.toEntity(dto, user);
        Education saved = educationRepository.save(education);

        return mapper.toDTO(saved);
    }

    public EducationDTO getEducationById(Long userId, Long id) {
        userService.getUserById(userId);

        Education education = educationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EducationNotFoundException(
                        String.format("Education with id %d not found for user %d", id, userId)
                ));

        return mapper.toDTO(education);
    }

    public List<EducationDTO> getAllEducation(Long userId) {
        userService.getUserById(userId); // Verify user exists
        return educationRepository.findByUserIdOrderByStartDateDesc(userId)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteEducation(Long userId, Long id) {
        // Проверяем существование пользователя
        userService.getUserById(userId);

        // Находим образование и проверяем права доступа
        Education education = educationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EducationNotFoundException(
                        String.format("Education with id %d not found for user %d", id, userId)
                ));

        // Удаляем
        educationRepository.delete(education);

        log.info("Deleted education with id {} for user {}", id, userId);
    }
}


