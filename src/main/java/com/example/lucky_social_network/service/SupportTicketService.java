package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.Admin;
import com.example.lucky_social_network.model.SupportTicket;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportTicketService {
    private final SupportTicketRepository supportTicketRepository;
    private final UserService userService;


    @Transactional
    public SupportTicket createTicket(User user, String subject, String message) {
        // Проверяем входные данные
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Некорректный пользователь");
        }

        // Получаем актуального пользователя из базы данных
        User actualUser = userService.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Тема не может быть пустой");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Сообщение не может быть пустым");
        }

        // Создаем новый тикет
        SupportTicket ticket = new SupportTicket();
        ticket.setUser(actualUser);  // Используем пользователя из базы данных
        ticket.setSubject(subject.trim());
        ticket.setMessage(message.trim());
        ticket.setStatus(SupportTicket.TicketStatus.OPEN);
        ticket.setCreatedAt(LocalDateTime.now());

        // Сохраняем и возвращаем
        return supportTicketRepository.save(ticket);
    }

    public List<SupportTicket> getAllTickets() {
        return supportTicketRepository.findAll();
    }

    public List<SupportTicket> getOpenTickets() {
        return supportTicketRepository.findByStatus(SupportTicket.TicketStatus.OPEN);
    }

    public SupportTicket updateTicketStatus(Long ticketId, SupportTicket.TicketStatus newStatus, Admin admin) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(newStatus);
        ticket.setAssignedAdmin(admin);
        return supportTicketRepository.save(ticket);
    }

    public List<SupportTicket> getTicketsByUser(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return supportTicketRepository.findByUser(user);
    }

    public SupportTicket getTicketByUserAndId(Long userId, Long ticketId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return supportTicketRepository.findByUserAndId(user, ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found for this user"));
    }

    public Page<SupportTicket> getTicketsByUserPaginated(Long userId, int page, int size) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return supportTicketRepository.findByUser(user, pageable);
    }

    public Page<SupportTicket> getAllTicketsPaginated(int page, int size) {
        return supportTicketRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public void addUserReply(Long ticketId, Long userId, String replyMessage) {
        SupportTicket ticket = getTicketByUserAndId(userId, ticketId);

        // Добавляем новый ответ к существующему сообщению
        String updatedMessage = ticket.getMessage() + "\n\nUser Reply (" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) +
                "):\n" + replyMessage;

        ticket.setMessage(updatedMessage);
        ticket.setStatus(SupportTicket.TicketStatus.IN_PROGRESS); // Обновляем статус
        supportTicketRepository.save(ticket);
    }

    public void addAdminResponse(Long ticketId, Admin admin, String response) {
        SupportTicket ticket = getTicketById(ticketId);

        // Добавляем временную метку к ответу администратора
        String formattedResponse = String.format(
                "[%s] Admin Response:\n%s",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                response
        );

        // Если уже есть предыдущий ответ, добавляем новый ответ с разделителем
        if (ticket.getAdminResponse() != null && !ticket.getAdminResponse().isEmpty()) {
            ticket.setAdminResponse(ticket.getAdminResponse() + "\n\n" + formattedResponse);
        } else {
            ticket.setAdminResponse(formattedResponse);
        }

        ticket.setAssignedAdmin(admin);

        // Обновляем статус, если тикет был открыт
        if (ticket.getStatus() == SupportTicket.TicketStatus.OPEN) {
            ticket.setStatus(SupportTicket.TicketStatus.IN_PROGRESS);
        }

        supportTicketRepository.save(ticket);
    }

    public SupportTicket getTicketById(Long ticketId) {
        return supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

    }

    public long countTicketsByStatus(SupportTicket.TicketStatus status) {
        return supportTicketRepository.countByStatus(status);
    }

    public void deleteticketId(Long ticketId) {
        supportTicketRepository.deleteById(ticketId);
    }
}
