package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.UserInterests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInterestsRepository extends JpaRepository<UserInterests, Long> {
    Optional<UserInterests> findByUserId(Long userId);

}
