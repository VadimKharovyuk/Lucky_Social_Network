package com.example.lucky_social_network.dto;

import lombok.Data;

@Data
public class UserInterestsDTO {
    private Long id;
    private Long userId;

    private String favoriteMusic;
    private String favoriteMovies;
    private String favoriteTvShows;
    private String favoriteAuthors;
    private String sportsActivities;
    private String hobbies;
    private String skills;
    private String visitedPlaces;
    private String techInterests;
    private String artInterests;
    private String learningInterests;
    private String languages;
    private String favoriteQuotes;
}