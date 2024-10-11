package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public User registerNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDate.now());
        user.setEmailVerified(false);
        user.setIsPrivate(false);
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
}