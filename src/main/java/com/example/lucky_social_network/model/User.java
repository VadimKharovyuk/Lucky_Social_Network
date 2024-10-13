package com.example.lucky_social_network.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;
    private String phone;
    @Column(length = 500)
    private String bio;

    // Используем bytea для бинарных данных
    @Lob
    private byte[] avatar;

    private LocalDate createdAt;

    private LocalDate dateOfBirth;
    private Boolean emailVerified;


    private Boolean isPrivate;
    private LocalDateTime lastLogin;
    private String location;


    @Column(nullable = false)
    private Integer friendsCount = 0;

    @Column(nullable = false)
    private Integer followersCount = 0;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<User> friends = new HashSet<>();


    public void removeFriend(User friend) {
        this.friends.remove(friend);
        friend.getFriends().remove(this);
    }


    public User(Long id) {
        this.id = id;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    public enum Gender {
        MALE,
        FEMALE
    }

    @ElementCollection
    @CollectionTable(name = "user_relationship_status", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "status")
    private List<String> relationshipStatus = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "partner_id")
    private User partner;

}