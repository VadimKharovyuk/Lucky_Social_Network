package com.example.lucky_social_network.redis;

import com.example.lucky_social_network.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDate;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class UserCacheDTO implements Serializable {
    private static final long serialVersionUID = 1L;


    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    @JsonProperty("avatarUrl")
    private String avatarUrl;
    private String bio;
    private LocalDate dateOfBirth;
    private String location;
    private String statusMessage;

    
    // Базовые настройки профиля
    private Boolean isPrivate;
    private Boolean emailVerified;
    private String gender;

    // Счетчики активности
    private Integer friendsCount;
    private Integer followersCount;


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
//        dto.setStatusMessage(user.getStatusMessage());

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

        return dto;
    }


}
