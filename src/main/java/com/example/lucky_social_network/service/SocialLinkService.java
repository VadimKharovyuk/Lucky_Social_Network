package com.example.lucky_social_network.service;

import com.example.lucky_social_network.dto.SocialLinkCreateDTO;
import com.example.lucky_social_network.dto.SocialLinkResponseDTO;
import com.example.lucky_social_network.dto.SocialLinkUpdateDTO;

import java.util.List;

public interface SocialLinkService {
    SocialLinkResponseDTO create(SocialLinkCreateDTO dto);

    SocialLinkResponseDTO update(SocialLinkUpdateDTO dto);

    void delete(Long id);

    SocialLinkResponseDTO getById(Long id);

    List<SocialLinkResponseDTO> getAllByUserId(Long userId);
}
