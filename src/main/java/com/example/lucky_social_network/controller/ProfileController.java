package com.example.lucky_social_network.controller;

import com.dropbox.core.DbxException;
import com.example.lucky_social_network.dto.PostCreationDto;
import com.example.lucky_social_network.model.Notification;
import com.example.lucky_social_network.model.Post;
import com.example.lucky_social_network.model.RelationshipStatusConstants;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final PostService postService;
    private final DropboxService dropboxService;
    private final NotificationService notificationService;


    @GetMapping("/{userId}")
    public String getUserProfile(@PathVariable Long userId, Model model) {

        try {
            User user = userService.getUserProfileById(userId);
            Long currentUserId = getCurrentUserId();
            User currentUser = userService.getUserById(currentUserId);

            // Получаем URL аватара пользователя
            String avatarUrl = userService.getUserAvatarUrl(user);

            boolean areFriends = userService.areFriends(currentUserId, userId);
            List<Post> userPosts = postService.getPostsByAuthor(user);

            // Получаем количество непрочитанных уведомлений
            long unreadNotificationCount = notificationService.getUnreadNotificationCount(currentUserId);

            // Получаем список уведомлений для текущего пользователя
            List<Notification> notifications = notificationService.getUserNotifications(currentUserId);

            LocalDate today = LocalDate.now();
            if (user.getDateOfBirth() != null &&
                    user.getDateOfBirth().getMonth() == today.getMonth() &&
                    user.getDateOfBirth().getDayOfMonth() == today.getDayOfMonth()) {
                model.addAttribute("isBirthday", true);
            }

            model.addAttribute("currentUser", currentUser);
            model.addAttribute("areFriends", areFriends);
            model.addAttribute("user", user);
            model.addAttribute("avatarUrl", avatarUrl);
            model.addAttribute("postCreationDto", new PostCreationDto());
            model.addAttribute("userPosts", userPosts);
            model.addAttribute("notificationCount", unreadNotificationCount);
            model.addAttribute("notifications", notifications);
            model.addAttribute("isEmailVerified", user.getEmailVerified());


            return "user-profile";
        } catch (Exception e) {
            log.error("Error processing profile request for userId: " + userId, e);
            throw new RuntimeException("Error processing profile request", e);
        }
    }

    //профиль пользователя
    @GetMapping
    public String getProfile(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());

        String avatarUrl = userService.getUserAvatarUrl(user);

        // Добавляем URL аватара в модель, если он существует
        if (avatarUrl != null) {
            model.addAttribute("avatarUrl", avatarUrl);
        }
        model.addAttribute("avatarUrl", avatarUrl);
        model.addAttribute("relationshipStatuses", RelationshipStatusConstants.getAllStatuses());
        model.addAttribute("user", user);
        return "profile";
    }

//обновиить профиль

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User updatedUser,
                                @RequestParam("avatarFile") MultipartFile avatarFile,
                                @RequestParam(required = false) String relationshipStatus,
                                @RequestParam(required = false) Long partnerId,
                                RedirectAttributes redirectAttributes) {
        try {
            // Обновляем основную информацию пользователя
            userService.updateUser(updatedUser, relationshipStatus, partnerId);

            // Если пользователь загрузил новый аватар, обновляем его
            if (!avatarFile.isEmpty()) {
                String dropboxPath = dropboxService.uploadFile(avatarFile);
                userService.updateAvatarDropboxPath(updatedUser.getId(), dropboxPath);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Профиль успешно обновлен.");
            return "redirect:/profile";
        } catch (IOException e) {
            log.error("Ошибка при загрузке аватара: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при загрузке аватара.");
            return "redirect:/profile?error=avatar_upload_failed";
        } catch (DbxException e) {
            log.error("Ошибка при загрузке аватара в Dropbox: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при сохранении аватара.");
            return "redirect:/profile?error=dropbox_upload_failed";
        }
    }


    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            return userDetails.getId();
        }
        throw new IllegalStateException("User not authenticated or CustomUserDetails not found");
    }
}
