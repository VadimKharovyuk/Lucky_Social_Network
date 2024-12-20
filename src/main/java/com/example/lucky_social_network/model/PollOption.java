package com.example.lucky_social_network.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "poll_options")
@Data
//Опция опроса
public class PollOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "poll_id")
    private Poll poll;

    private String text;
    private Long votesCount = 0L;
    // Добавляем поле position для сортировки вариантов
    @Column(name = "position")
    private Integer position;

    @ManyToMany
    @JoinTable(name = "poll_votes")
    private Set<User> voters = new HashSet<>();
}
