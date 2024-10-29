package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.exception.UnauthorizedAccessException;
import com.example.lucky_social_network.model.Admin;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.AdminService;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Slf4j

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final UserService userService;


    @GetMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String adminPanel(
            @RequestParam(required = false) String searchQuery,
            Model model
    ) {
        List<Admin> admins = adminService.getAllAdmins();

        // Если есть поисковый запрос, ищем пользователей
        List<User> users = searchQuery != null && !searchQuery.isEmpty()
                ? userService.searchUsers(searchQuery)
                : Collections.emptyList();

        model.addAttribute("admins", admins);
        model.addAttribute("users", users);
        model.addAttribute("searchQuery", searchQuery);
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


    @GetMapping("/search-users")
    public String searchUsers(@RequestParam(required = false) String userSearchQuery,
                              Model model) {
        try {
            // Получаем текущего админа для проверки прав
            User currentAdmin = userService.getCurrentUser();
            Admin admin = adminService.getAdminByUserId(currentAdmin.getId());

            // Поиск пользователей
            List<User> searchedUsers = userSearchQuery != null && !userSearchQuery.isEmpty()
                    ? userService.searchUsers(userSearchQuery)
                    : Collections.emptyList();

            // Добавляем атрибуты для результатов поиска
            model.addAttribute("searchedUsers", searchedUsers);
            model.addAttribute("userSearchQuery", userSearchQuery);

            // Добавляем общие атрибуты для страницы
            addCommonAttributes(model, null);

            // Добавляем сообщение о результатах поиска
            if (userSearchQuery != null && !userSearchQuery.isEmpty()) {
                if (searchedUsers.isEmpty()) {
                    model.addAttribute("searchMessage",
                            "По запросу '" + userSearchQuery + "' пользователей не найдено");
                } else {
                    model.addAttribute("searchMessage",
                            "Найдено пользователей: " + searchedUsers.size());
                }
            }

            log.info("Search users query: {}, found: {}",
                    userSearchQuery, searchedUsers.size());

            return "admin/panel";
        } catch (Exception e) {
            log.error("Error searching users: ", e);
            model.addAttribute("errorMessage",
                    "Ошибка при поиске пользователей: " + e.getMessage());
            addCommonAttributes(model, null);
            return "admin/panel";
        }
    }

    @PostMapping("/delete-user")
    public String deleteUser(@RequestParam Long userId,
                             RedirectAttributes redirectAttributes) {
        try {
            // Получаем текущего админа
            User currentAdmin = userService.getCurrentUser();
            Admin admin = adminService.getAdminByUserId(currentAdmin.getId());

            // Получаем информацию о пользователе перед удалением
            User userToDelete = userService.getUserById(userId);
            String username = userToDelete.getUsername();

            // Удаляем пользователя
            adminService.deleteUserProfile(admin.getId(), userId);

            // Добавляем подробное сообщение об успехе
            redirectAttributes.addFlashAttribute("successMessage",
                    "Пользователь " + username + " успешно удален");

            // Сохраняем поисковый запрос для восстановления результатов
            redirectAttributes.addFlashAttribute("userSearchQuery",
                    redirectAttributes.getAttribute("userSearchQuery"));

        } catch (UnauthorizedAccessException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Недостаточно прав для удаления пользователя");
        } catch (Exception e) {
            log.error("Error deleting user {}: ", userId, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении пользователя: " + e.getMessage());
        }

        return "redirect:/admin";
    }

    // Общий метод для добавления базовых атрибутов в модель
    private void addCommonAttributes(Model model, String searchQuery) {
        List<Admin> admins = adminService.getAllAdmins();
        model.addAttribute("admins", admins);
        model.addAttribute("adminRoles", Admin.AdminRole.values());
        model.addAttribute("searchQuery", searchQuery); // для основной формы поиска
    }

}