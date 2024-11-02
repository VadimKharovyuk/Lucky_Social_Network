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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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


    private static final String SOCIAL_LINK_CACHE = "socialLink";
    private static final String USER_SOCIAL_LINKS_CACHE = "userSocialLinks";

    @Override
    @Caching(put = {
            @CachePut(value = SOCIAL_LINK_CACHE, key = "#result.id")
    })
    @CacheEvict(value = USER_SOCIAL_LINKS_CACHE, key = "#dto.userId")  // Только очищаем кэш списка
    public SocialLinkResponseDTO create(SocialLinkCreateDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));

        if (socialLinkRepository.existsByUserIdAndPlatform(dto.getUserId(), dto.getPlatform())) {
            throw new DuplicateResourceException("Social link for platform " + dto.getPlatform() + " already exists");
        }

        SocialLink socialLink = socialLinkMapper.toEntity(dto);
        socialLink.setUser(user);
        socialLink.setCreatedAt(LocalDateTime.now());
        socialLink.setUpdatedAt(LocalDateTime.now());

        SocialLink saved = socialLinkRepository.save(socialLink);
        return socialLinkMapper.toResponseDto(saved);
    }
    @Override
    @Caching(put = {
            @CachePut(value = SOCIAL_LINK_CACHE, key = "#result.id")
    })
    @CacheEvict(value = USER_SOCIAL_LINKS_CACHE, allEntries = true)  // Очищаем весь кэш списков
    public SocialLinkResponseDTO update(SocialLinkUpdateDTO dto) {
        SocialLink socialLink = socialLinkRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Social link not found with id: " + dto.getId()));

        socialLinkMapper.updateEntityFromDto(dto, socialLink);
        socialLink.setUpdatedAt(LocalDateTime.now());

        SocialLink updated = socialLinkRepository.save(socialLink);
        return socialLinkMapper.toResponseDto(updated);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = SOCIAL_LINK_CACHE, key = "#id"),
            @CacheEvict(value = USER_SOCIAL_LINKS_CACHE, allEntries = true)
    })
    public void delete(Long id) {
        if (!socialLinkRepository.existsById(id)) {
            throw new ResourceNotFoundException("Social link not found with id: " + id);
        }
        socialLinkRepository.deleteById(id);
    }

    @Override
    @Cacheable(value = SOCIAL_LINK_CACHE, key = "#id")
    public SocialLinkResponseDTO getById(Long id) {
        SocialLink socialLink = socialLinkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Social link not found with id: " + id));
        return socialLinkMapper.toResponseDto(socialLink);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = USER_SOCIAL_LINKS_CACHE, key = "#userId")
    public List<SocialLinkResponseDTO> getAllByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return socialLinkRepository.findByUserId(userId).stream()
                .map(socialLinkMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}
