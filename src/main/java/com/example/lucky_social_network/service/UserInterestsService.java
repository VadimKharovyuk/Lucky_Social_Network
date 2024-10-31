package com.example.lucky_social_network.service;

import com.example.lucky_social_network.dto.UserInterestsDTO;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.model.UserInterests;
import com.example.lucky_social_network.repository.UserInterestsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class UserInterestsService {

    private final UserInterestsRepository userInterestsRepository;
    private final UserService userService;

    @Transactional
    public UserInterestsDTO createUserInterests(Long userId, UserInterestsDTO dto) {
        User user = userService.getUserById(userId);

        UserInterests interests = new UserInterests();
        interests.setUser(user);
        updateInterestsFromDTO(interests, dto);

        UserInterests saved = userInterestsRepository.save(interests);
        log.info("Created interests for user: {}", userId);

        return convertToDTO(saved);
    }

    @Transactional
    public UserInterestsDTO updateUserInterests(Long userId, UserInterestsDTO dto) {
        UserInterests interests = userInterestsRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Interests not found"));

        if (!interests.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        updateInterestsFromDTO(interests, dto);
        UserInterests saved = userInterestsRepository.save(interests);
        log.info("Updated interests for user: {}", userId);

        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public UserInterestsDTO getUserInterests(Long userId) {
        UserInterests interests = userInterestsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Interests not found for user: " + userId));
        return convertToDTO(interests);
    }

    @Transactional
    public void deleteUserInterests(Long userId, Long interestsId) {
        UserInterests interests = userInterestsRepository.findById(interestsId)
                .orElseThrow(() -> new RuntimeException("Interests not found"));

        if (!interests.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        userInterestsRepository.delete(interests);
        log.info("Deleted interests for user: {}", userId);
    }

    // Вспомогательные методы
    private void updateInterestsFromDTO(UserInterests interests, UserInterestsDTO dto) {
        interests.setFavoriteMusic(dto.getFavoriteMusic());
        interests.setFavoriteMovies(dto.getFavoriteMovies());
        interests.setFavoriteTvShows(dto.getFavoriteTvShows());
        interests.setFavoriteAuthors(dto.getFavoriteAuthors());
        interests.setSportsActivities(dto.getSportsActivities());
        interests.setHobbies(dto.getHobbies());
        interests.setSkills(dto.getSkills());
        interests.setVisitedPlaces(dto.getVisitedPlaces());
        interests.setTechInterests(dto.getTechInterests());
        interests.setArtInterests(dto.getArtInterests());
        interests.setLearningInterests(dto.getLearningInterests());
        interests.setLanguages(dto.getLanguages());
        interests.setFavoriteQuotes(dto.getFavoriteQuotes());
    }

    private UserInterestsDTO convertToDTO(UserInterests interests) {
        UserInterestsDTO dto = new UserInterestsDTO();
        dto.setId(interests.getId());
        dto.setUserId(interests.getUser().getId());
        dto.setFavoriteMusic(interests.getFavoriteMusic());
        dto.setFavoriteMovies(interests.getFavoriteMovies());
        dto.setFavoriteTvShows(interests.getFavoriteTvShows());
        dto.setFavoriteAuthors(interests.getFavoriteAuthors());
        dto.setSportsActivities(interests.getSportsActivities());
        dto.setHobbies(interests.getHobbies());
        dto.setSkills(interests.getSkills());
        dto.setVisitedPlaces(interests.getVisitedPlaces());
        dto.setTechInterests(interests.getTechInterests());
        dto.setArtInterests(interests.getArtInterests());
        dto.setLearningInterests(interests.getLearningInterests());
        dto.setLanguages(interests.getLanguages());
        dto.setFavoriteQuotes(interests.getFavoriteQuotes());
        return dto;
    }
}
