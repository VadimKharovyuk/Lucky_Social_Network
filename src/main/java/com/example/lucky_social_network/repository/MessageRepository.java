package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Message;
import com.example.lucky_social_network.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m " +
            "WHERE ((m.sender.id = :userId AND m.deletedForSender = false) OR " +
            "(m.recipient.id = :userId AND m.deletedForRecipient = false)) AND " +
            "((m.sender.id = :partnerId) OR (m.recipient.id = :partnerId)) " +
            "ORDER BY m.timestamp")
    List<Message> findAvailableChatHistory(@Param("userId") Long userId,
                                           @Param("partnerId") Long partnerId);


    @Modifying
    @Query("DELETE FROM Message m WHERE m.sender = :user OR m.recipient = :user")
    void deleteAllBySenderOrRecipient(@Param("user") User user);



    List<Message> findBySenderOrRecipient(User sender, User recipient);


}

