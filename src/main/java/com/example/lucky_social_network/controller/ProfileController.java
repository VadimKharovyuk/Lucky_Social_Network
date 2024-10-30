package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.PostCreationDto;
import com.example.lucky_social_network.model.Album;
import com.example.lucky_social_network.model.Notification;
import com.example.lucky_social_network.model.Post;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.redis.UserCacheDTO;
import com.example.lucky_social_network.service.*;
import com.example.lucky_social_network.service.picService.ImgurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final PostService postService;
    private final NotificationService notificationService;
    private final ImgurService imgurService;
    private final AlbumService albumService;


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
        model.addAttribute("user", user);
        return "profile";
    }


    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User updatedUser,
                                @RequestParam("avatarFile") MultipartFile avatarFile,
                                RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser();
        try {
            // Обновляем основную информацию пользователя
            userService.updateUser(updatedUser);

            // Если пользователь загрузил новый аватар, обновляем его
            if (!avatarFile.isEmpty()) {
                byte[] imageData = avatarFile.getBytes();
                String imageUrl = imgurService.uploadImage(imageData);
                if (imageUrl != null) {
                    userService.updateAvatarUrl(updatedUser.getId(), imageUrl);
                } else {
                    throw new RuntimeException("Не удалось получить URL изображения от Imgur");
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Профиль успешно обновлен.");
            return "redirect:/settings";
        } catch (IOException e) {
            log.error("Ошибка при чтении файла аватара: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при загрузке аватара.");
            return "redirect:/profile?error=avatar_upload_failed";
        } catch (Exception e) {
            log.error("Ошибка при обновлении профиля: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении профиля.");
            return "redirect:/profile?error=profile_update_failed";
        }
    }

    @GetMapping("/{userId}")
    public String getUserProfile(@PathVariable Long userId,
                                 @PageableDefault(size = 6) Pageable albumPageable,
                                 Model model) {
        try {
            // Получаем кэшированные базовые данные
            UserCacheDTO userProfile = userService.getUserProfileById(userId);

            // Получаем полную сущность для операций, требующих дополнительные данные
            User fullUser = userService.getUserFullProfile(userId);

            Long currentUserId = getCurrentUserId();
            User currentUser = userService.getUserFullProfile(currentUserId);

            // Проверка дня рождения перенесена в сервис
            boolean isBirthday = userService.isBirthdayToday(userId);
            if (isBirthday) {
                model.addAttribute("isBirthday", true);
            }

            // Получаем данные для отображения
            boolean areFriends = userService.areFriends(currentUserId, userId);

            List<Post> userPosts = postService.getPostsByAuthor(fullUser);
            long unreadNotificationCount = notificationService.getUnreadNotificationCount(currentUserId);
            List<Notification> notifications = notificationService.getUserNotifications(currentUserId);
            Page<Album> userAlbums = albumService.getPublicAlbumsByUserId(userId, albumPageable);


            boolean isFriend = userService.areFriends(currentUserId, userId);

            String onlineStatus = userService.getUserOnlineStatus(userId);
            model.addAttribute("onlineStatus", onlineStatus);

            log.info("Получен пользователь: {}", fullUser.getUsername());
            log.info("Время последнего входа: {}", fullUser.getLastLogin());
            log.info("Статус онлайн: {}", onlineStatus);


            // Добавляем атрибуты в модель
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("areFriends", areFriends);
            model.addAttribute("user", userProfile);
            model.addAttribute("avatarUrl", userProfile.getAvatarUrl());
            model.addAttribute("postCreationDto", new PostCreationDto());
            model.addAttribute("userPosts", userPosts);
            model.addAttribute("notificationCount", unreadNotificationCount);
            model.addAttribute("notifications", notifications);
            model.addAttribute("isEmailVerified", userProfile.getEmailVerified());

            model.addAttribute("albums", userAlbums.getContent());
            model.addAttribute("totalAlbums", userAlbums.getTotalElements());
            model.addAttribute("totalPages", userAlbums.getTotalPages());
            model.addAttribute("currentPage", userAlbums.getNumber());
            model.addAttribute("isFriend", isFriend);
            model.addAttribute("user", userProfile);
            model.addAttribute("onlineStatus", onlineStatus);


            return "user-profile";
        } catch (Exception e) {
            log.error("Error processing profile request for userId: " + userId, e);
            throw new RuntimeException("Error processing profile request", e);
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
