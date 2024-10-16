package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Notification;
import com.example.lucky_social_network.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    long countByUserAndIsReadFalse(User user);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

}
