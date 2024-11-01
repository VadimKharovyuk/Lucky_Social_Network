package com.example.lucky_social_network.service;

import com.example.lucky_social_network.dto.SocialLinkCreateDTO;
import com.example.lucky_social_network.dto.SocialLinkResponseDTO;
import com.example.lucky_social_network.dto.SocialLinkUpdateDTO;
import com.example.lucky_social_network.exception.DuplicateResourceException;
import com.example.lucky_social_network.exception.ResourceNotFoundException;
import com.example.lucky_social_network.maper.SocialLinkMapper;
import com.example.lucky_social_network.model.SocialLink;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.SocialLinkRepository;
import com.example.lucky_social_network.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SocialLinkServiceImpl implements SocialLinkService {
    private final SocialLinkRepository socialLinkRepository;
    private final SocialLinkMapper socialLinkMapper;
    private final UserRepository userRepository;

    @Override
    public SocialLinkResponseDTO create(SocialLinkCreateDTO dto) {
        // Проверяем существование пользователя
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));

        // Проверяем, нет ли уже такой платформы у пользователя
        if (socialLinkRepository.existsByUserIdAndPlatform(dto.getUserId(), dto.getPlatform())) {
            throw new DuplicateResourceException("Social link for platform " + dto.getPlatform() + " already exists");
        }

        // Создаем новую ссылку
        SocialLink socialLink = socialLinkMapper.toEntity(dto);
        socialLink.setUser(user);
        socialLink.setCreatedAt(LocalDateTime.now());
        socialLink.setUpdatedAt(LocalDateTime.now());

        // Сохраняем и возвращаем DTO
        SocialLink saved = socialLinkRepository.save(socialLink);
        return socialLinkMapper.toResponseDto(saved);
    }

    @Override
    public SocialLinkResponseDTO update(SocialLinkUpdateDTO dto) {
        SocialLink socialLink = socialLinkRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Social link not found with id: " + dto.getId()));

        // Обновляем поля
        socialLinkMapper.updateEntityFromDto(dto, socialLink);
        socialLink.setUpdatedAt(LocalDateTime.now());

        // Сохраняем и возвращаем обновленные данные
        SocialLink updated = socialLinkRepository.save(socialLink);
        return socialLinkMapper.toResponseDto(updated);
    }

    @Override
    public void delete(Long id) {
        if (!socialLinkRepository.existsById(id)) {
            throw new ResourceNotFoundException("Social link not found with id: " + id);
        }
        socialLinkRepository.deleteById(id);
    }

    @Override
    public SocialLinkResponseDTO getById(Long id) {
        SocialLink socialLink = socialLinkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Social link not found with id: " + id));
        return socialLinkMapper.toResponseDto(socialLink);
    }

    @Transactional(readOnly = true)
    public List<SocialLinkResponseDTO> getAllByUserId(Long userId) {
        // Проверяем существование пользователя простым запросом
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return socialLinkRepository.findByUserId(userId).stream()
                .map(socialLinkMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}
