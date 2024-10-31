package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.model.UserDailyActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserDailyActivityRepository extends JpaRepository<UserDailyActivity, Long> {

    Optional<UserDailyActivity> findByUserAndDate(User user, LocalDate date);

    List<UserDailyActivity> findByUserOrderByDateDesc(User user);

}
