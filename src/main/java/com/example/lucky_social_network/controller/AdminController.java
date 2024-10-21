package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.Admin;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.AdminService;
import com.example.lucky_social_network.service.SupportTicketService;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final UserService userService;
    private final SupportTicketService supportTicketService;

    @GetMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String adminPanel(Model model) {
        List<Admin> admins = adminService.getAllAdmins();
        List<User> users = userService.getAllUsers();
        model.addAttribute("admins", admins);
        model.addAttribute("users", users);
        model.addAttribute("adminRoles", Admin.AdminRole.values());
        return "admin/panel";
    }

    @PostMapping("/convert")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String convertUserToAdmin(@RequestParam Long userId,
                                     @RequestParam Admin.AdminRole role) {
        adminService.convertUserToAdmin(userId, role);
        return "redirect:/admin";
    }

    @PostMapping("/update-role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String updateAdminRole(@RequestParam Long adminId,
                                  @RequestParam Admin.AdminRole newRole) {
        adminService.updateAdminRole(adminId, newRole);
        return "redirect:/admin";
    }

    @PostMapping("/remove")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String removeAdmin(@RequestParam Long adminId) {
        adminService.removeAdmin(adminId);
        return "redirect:/admin";
    }


}