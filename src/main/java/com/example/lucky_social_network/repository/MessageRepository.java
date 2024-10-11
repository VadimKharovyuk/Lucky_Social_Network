package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :senderId AND m.recipient.id = :recipientId) " +
            "OR (m.sender.id = :recipientId AND m.recipient.id = :senderId) ORDER BY m.timestamp")
    List<Message> findChatHistory(@Param("senderId") Long senderId, @Param("recipientId") Long recipientId);



}
