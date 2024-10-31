package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.UserInterests;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInterestsRepository extends JpaRepository<UserInterests, Long> {
    Optional<UserInterests> findByUserId(Long userId);

}
