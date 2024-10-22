package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.SupportTicket;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.SupportTicketService;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user/tickets")
@RequiredArgsConstructor
public class UserTicketController {
    private final SupportTicketService supportTicketService;
    private final UserService userService;

    // Список всех тикетов пользователя
    @GetMapping
    public String getUserTickets(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        Page<SupportTicket> ticketsPage = supportTicketService.getTicketsByUserPaginated(user.getId(), page, size);

        model.addAttribute("tickets", ticketsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ticketsPage.getTotalPages());
        model.addAttribute("totalItems", ticketsPage.getTotalElements());

        return "support/tickets-list";
    }

    // Просмотр конкретного тикета
    @GetMapping("/{ticketId}")
    public String getTicketDetails(@AuthenticationPrincipal UserDetails userDetails,
                                   @PathVariable Long ticketId,
                                   Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        SupportTicket ticket = supportTicketService.getTicketByUserAndId(user.getId(), ticketId);
        model.addAttribute("ticket", ticket);
        return "support/ticket-details";
    }


//    @GetMapping("/form")
//    public String crateTicketForm(Model model) {
//        model.addAttribute("ticket", new SupportTicket());
//        return "support/ticket-form";
//    }
//
//
//    @PostMapping("/submit")
//    public String submitTicket(@AuthenticationPrincipal UserDetails userDetails,
//                               @ModelAttribute("ticket") SupportTicket ticket,
//                               BindingResult bindingResult,
//                               RedirectAttributes redirectAttributes) {
//        if (bindingResult.hasErrors()) {
//            return "support/ticket-form";
//        }
//
//        try {
//            // Получаем текущего пользователя из базы данных
//            User user = userService.getUserByUsername(userDetails.getUsername());
//
//            // Создаем тикет с существующим пользователем
//            SupportTicket createdTicket = supportTicketService.createTicket(
//                    user,
//                    ticket.getSubject(),
//                    ticket.getMessage()
//            );
//
//            redirectAttributes.addFlashAttribute("message",
//                    "Тикет #" + createdTicket.getId() + " успешно создан");
//
//            return "redirect:/user/tickets";
//
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error",
//                    "Ошибка при создании тикета: " + e.getMessage());
//            return "redirect:/user/tickets/create";
//        }
//    }
}