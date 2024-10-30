package com.example.lucky_social_network.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String firstName;

    @Column(length = 50)
    private String lastName;


    private String email;

    private String phone;
    @Column(length = 500)
    private String bio;


    @Column(name = "avatar_dropbox_path")
    private String avatarUrl;


    private LocalDate createdAt;

    private LocalDate dateOfBirth;
    private Boolean emailVerified;


    private Boolean isPrivate;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;


//    public LocalDateTime getLastLogin() {
//        return lastLogin;
//    }
//
//    public void setLastLogin(LocalDateTime lastLogin) {
//        this.lastLogin = lastLogin;
//    }

//    @PrePersist
//    public void prePersist() {
//        if (this.createdAt == null) {
//            this.createdAt = LocalDate.now();
//        }
//    }

    private String location;


    @Column(nullable = false)
    private Integer friendsCount = 0;

    @Column(nullable = false)
    private Integer followersCount = 0;


    @JsonIgnore
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

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;


    public enum Gender {
        MALE,
        FEMALE
    }

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "partner_id")
    private User partner;


    @JsonIgnore
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Subscription> subscriptions = new HashSet<>();
    @JsonIgnore
    @OneToMany(mappedBy = "followee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Subscription> followers = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private Set<Role> roles = new HashSet<>();

    // Добавим геттер с логированием
    public Set<Role> getRoles() {
        log.debug("Getting roles for user {}: {}", this.id, this.roles);
        return this.roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public enum Role {
        USER,
        ADMIN,
        MODERATOR

    }

    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Album> albums = new HashSet<>();
    @JsonIgnore
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Photo> photos = new HashSet<>();

}