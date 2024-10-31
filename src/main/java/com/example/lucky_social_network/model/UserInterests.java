package com.example.lucky_social_network.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_interests")
@Data
public class UserInterests {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    // Музыкальные предпочтения
    @Column(name = "favorite_music", length = 500)
    private String favoriteMusic;  // Рок, Джаз, Классика

    // Кино и ТВ
    @Column(name = "favorite_movies", length = 500)
    private String favoriteMovies;

    @Column(name = "favorite_tv_shows", length = 500)
    private String favoriteTvShows;


    @Column(name = "favorite_authors", length = 500)
    private String favoriteAuthors;


    // Спорт и активности
    @Column(name = "sports_activities", length = 500)
    private String sportsActivities;  // Футбол, Йога, Бег


    // Хобби и увлечения
    @Column(name = "hobbies", length = 500)
    private String hobbies;  // Фотография, Кулинария, Путешествия

    @Column(name = "skills", length = 500)
    private String skills;  // Навыки и умения


    // Путешествия
    @Column(name = "visited_places", length = 500)
    private String visitedPlaces;


    // Технологии и гаджеты
    @Column(name = "tech_interests", length = 500)
    private String techInterests;  // Программирование, AI, Гаджеты

    // Искусство и культура
    @Column(name = "art_interests", length = 500)
    private String artInterests;  // Живопись, Театр, Фотография

    // Образование и развитие
    @Column(name = "learning_interests", length = 500)
    private String learningInterests;  // Что хочет изучить

    @Column(name = "languages", length = 500)
    private String languages;  // Языки, которые знает или учит

    // Цитаты и мотивация
    @Column(name = "favorite_quotes", length = 1000)
    private String favoriteQuotes;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
