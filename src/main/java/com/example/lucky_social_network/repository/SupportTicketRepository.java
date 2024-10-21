package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.SupportTicket;
import com.example.lucky_social_network.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByStatus(SupportTicket.TicketStatus ticketStatus);


    List<SupportTicket> findByUser(User user);

    Optional<SupportTicket> findByUserAndId(User user, Long id);

    Page<SupportTicket> findByUser(User user, Pageable pageable);

    long countByStatus(SupportTicket.TicketStatus status);
    
}
