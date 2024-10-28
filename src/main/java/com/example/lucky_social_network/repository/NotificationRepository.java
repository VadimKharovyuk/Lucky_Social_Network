package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Notification;
import com.example.lucky_social_network.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    long countByUserAndIsReadFalse(User user);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);


    @Query("SELECT n FROM Notification n " +
            "LEFT JOIN FETCH n.comment c " +
            "LEFT JOIN FETCH c.author " +
            "WHERE n.user.id = :userId " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdWithDetails(@Param("userId") Long userId);

}
