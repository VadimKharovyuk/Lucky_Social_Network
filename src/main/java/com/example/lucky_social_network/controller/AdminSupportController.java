package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.Admin;
import com.example.lucky_social_network.model.SupportTicket;
import com.example.lucky_social_network.service.AdminService;
import com.example.lucky_social_network.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/support")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SENIOR_ADMIN', 'SUPER_ADMIN')")
public class AdminSupportController {
    private final SupportTicketService supportTicketService;
    private final AdminService adminService;

    @GetMapping("/tickets/{ticketId}")
    public String viewTicketDetails(@AuthenticationPrincipal UserDetails userDetails,
                                    @PathVariable Long ticketId,
                                    Model model) {
        try {
            // Проверяем права администратора
            Admin admin = adminService.getAdminByUsername(userDetails.getUsername());
            SupportTicket ticket = supportTicketService.getTicketById(ticketId);

            model.addAttribute("ticket", ticket);
            model.addAttribute("isAdmin", true);
            return "admin/ticket-details";
        } catch (Exception e) {
            model.addAttribute("error", "Access denied: " + e.getMessage());
            return "redirect:/error";
        }
    }

    // Ответ на тикет
    @PostMapping("/tickets/{ticketId}/reply")
    public String replyToTicket(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable Long ticketId,
                                @RequestParam String adminResponse,
                                RedirectAttributes redirectAttributes) {
        try {
            // Проверяем и получаем администратора
            Admin admin = adminService.getAdminByUsername(userDetails.getUsername());
            if (admin == null) {
                throw new RuntimeException("Admin privileges required to reply to tickets");
            }

            // Добавляем ответ
            supportTicketService.addAdminResponse(ticketId, admin, adminResponse);
            redirectAttributes.addFlashAttribute("message", "Ответ успешно отправлен");

        } catch (UsernameNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Пользователь не найден: " + e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при отправке ответа: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Произошла неизвестная ошибка: " + e.getMessage());
        }

        return "redirect:/admin/support/tickets/" + ticketId;
    }

    // Обновление статуса тикета
    @PostMapping("/update-status")
    public String updateTicketStatus(@AuthenticationPrincipal UserDetails userDetails,
                                     @RequestParam Long ticketId,
                                     @RequestParam SupportTicket.TicketStatus newStatus,
                                     RedirectAttributes redirectAttributes) {
        try {
            Admin admin = adminService.getAdminByUsername(userDetails.getUsername());
            if (admin == null) {
                throw new RuntimeException("Admin privileges required to update ticket status");
            }

            supportTicketService.updateTicketStatus(ticketId, newStatus, admin);
            redirectAttributes.addFlashAttribute("message", "Статус тикета успешно обновлен");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении статуса: " + e.getMessage());
        }

        return "redirect:/admin/support/tickets/" + ticketId;
    }

    // Список всех тикетов
    @GetMapping("/list")
    public String getAllTickets(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        Page<SupportTicket> ticketsPage = supportTicketService.getAllTicketsPaginated(page, size);

        model.addAttribute("tickets", ticketsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ticketsPage.getTotalPages());
        model.addAttribute("totalItems", ticketsPage.getTotalElements());

        // Добавляем статистику
        model.addAttribute("openTickets",
                supportTicketService.countTicketsByStatus(SupportTicket.TicketStatus.OPEN));
        model.addAttribute("inProgressTickets",
                supportTicketService.countTicketsByStatus(SupportTicket.TicketStatus.IN_PROGRESS));
        model.addAttribute("resolvedTickets",
                supportTicketService.countTicketsByStatus(SupportTicket.TicketStatus.RESOLVED));

        return "admin/support-list";
    }

}