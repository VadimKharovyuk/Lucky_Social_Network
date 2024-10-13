package com.example.lucky_social_network.service;

import com.example.lucky_social_network.exception.FriendshipNotFoundException;
import com.example.lucky_social_network.exception.UserNotFoundException;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.MessageRepository;
import com.example.lucky_social_network.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


//    public User updateUser(User user) {
//        return userRepository.save(user);
//    }

    public User updateUser(User user, String relationshipStatus, Long partnerId) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Обновляем основные поля
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setBio(user.getBio());
        existingUser.setDateOfBirth(user.getDateOfBirth());
        existingUser.setLocation(user.getLocation());
        existingUser.setLastLogin(user.getLastLogin());

        // Обновляем семейное положение
        if (relationshipStatus != null) {
            if (!relationshipStatus.isEmpty()) {
                existingUser.setRelationshipStatus(new ArrayList<>(Arrays.asList(relationshipStatus)));
            } else {
                existingUser.setRelationshipStatus(new ArrayList<>());
            }
        }

        // Обновляем партнера
        if (partnerId != null) {
            User partner = userRepository.findById(partnerId)
                    .orElseThrow(() -> new RuntimeException("Partner not found"));
            existingUser.setPartner(partner);
        } else {
            existingUser.setPartner(null);
        }

        return userRepository.save(existingUser);
    }


public User registerNewUser(User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setCreatedAt(LocalDate.now());
    user.setEmailVerified(false);
    user.setIsPrivate(false);
    user.setFollowersCount(0);
    user.setFriendsCount(0);
    return userRepository.save(user);
}

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }


    public User getUserById(Long senderId) {
       return userRepository.findById(senderId).orElseThrow(() -> new UsernameNotFoundException("User not found: " + senderId));
    }

    public List<User> getAllUsers() {
       return userRepository.findAll();
    }


    public void addFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (user.getFriends().contains(friend)) {
            throw new IllegalStateException("Users are already friends");
        }

        user.getFriends().add(friend);
        friend.getFriends().add(user);

        user.setFriendsCount(user.getFriendsCount() == null ? 1 : user.getFriendsCount() + 1);
        friend.setFollowersCount(friend.getFollowersCount() == null ? 1 : friend.getFollowersCount() + 1);

        userRepository.save(user);
        userRepository.save(friend);
    }

    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        log.info("Attempting to remove friend relationship between users: {} and {}", userId, friendId);

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (!user.getFriends().contains(friend)) {
            log.warn("Friend relationship not found between users: {} and {}", userId, friendId);
            throw new FriendshipNotFoundException("Дружба не найдена между пользователями");
        }

        // Удаляем друга из списка друзей
        user.removeFriend(friend);

        // Уменьшаем количество друзей у пользователя
        user.setFriendsCount(Math.max(0, user.getFriendsCount() - 1));
        // Уменьшаем количество подписчиков у друга
        friend.setFollowersCount(Math.max(0, friend.getFollowersCount() - 1));

        userRepository.save(user);
        userRepository.save(friend);

        log.info("Friend relationship successfully removed between users: {} and {}", userId, friendId);
    }


    @Transactional(readOnly = true)
    public Set<User> getFriendsByUser(Long userId) {
        User user = getUserById(userId);
        return user.getFriends();
    }

    public User getUserProfileById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
    }

    public boolean areFriends(Long userId1, Long userId2) {
        User user1 = getUserById(userId1);
        return user1.getFriends().stream().anyMatch(friend -> friend.getId().equals(userId2));
    }




}