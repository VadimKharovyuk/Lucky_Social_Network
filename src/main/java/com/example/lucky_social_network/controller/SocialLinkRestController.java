package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.SocialLinkCreateDTO;
import com.example.lucky_social_network.dto.SocialLinkResponseDTO;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.SocialLinkService;
import com.example.lucky_social_network.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/social-links")
@RequiredArgsConstructor
@Slf4j
public class SocialLinkRestController {
    private final SocialLinkService socialLinkService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<SocialLinkResponseDTO> create(@Valid @RequestBody SocialLinkCreateDTO dto) {
        try {
            User currentUser = userService.getCurrentUser();
            dto.setUserId(currentUser.getId());
            SocialLinkResponseDTO response = socialLinkService.create(dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating social link", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<SocialLinkResponseDTO>> getCurrentUserLinks() {
        try {
            User currentUser = userService.getCurrentUser();
            List<SocialLinkResponseDTO> links = socialLinkService.getAllByUserId(currentUser.getId());
            return ResponseEntity.ok(links);
        } catch (Exception e) {
            log.error("Error getting user links", e);
            return ResponseEntity.badRequest().build();
        }
    }
}