package com.example.lucky_social_network.redis;

import com.example.lucky_social_network.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class UserCacheDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // Основные данные профиля (часто используются, редко меняются)
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    @JsonProperty("avatarUrl")
    private String avatarUrl;
    private String bio;
    private LocalDate dateOfBirth;
    private String location;


    // Базовые настройки профиля
    private Boolean isPrivate;
    private Boolean emailVerified;
    private String gender;  // Оставляем, так как это может быть важно для UI

    // Счетчики активности
    private Integer friendsCount;
    private Integer followersCount;

    // Роли пользователя
    @JsonProperty("roles")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    @JsonSerialize(contentAs = String.class)
    private List<String> roles;

    public static UserCacheDTO fromUser(User user) {
        UserCacheDTO dto = new UserCacheDTO();

        // Основные данные
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setBio(user.getBio());
        
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setLocation(user.getLocation());

        // Настройки
        dto.setIsPrivate(user.getIsPrivate());
        dto.setEmailVerified(user.getEmailVerified());

        // Гендер
        if (user.getGender() != null) {
            dto.setGender(user.getGender().name());
        }

        // Счетчики
        dto.setFriendsCount(user.getFriendsCount());
        dto.setFollowersCount(user.getFollowersCount());

        // Роли
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<String> roleNames = user.getRoles().stream()
                    .map(Enum::name)
                    .toList();
            dto.setRoles(roleNames);
            log.debug("Setting roles for user {}: {}", user.getId(), roleNames);
        } else {
            log.debug("No roles found for user {}", user.getId());
        }

        return dto;
    }


}
