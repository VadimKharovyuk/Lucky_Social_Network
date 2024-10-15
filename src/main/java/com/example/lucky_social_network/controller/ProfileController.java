package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.PostCreationDto;
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
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final PostService postService;
//    private final ImageService imageService;


    //профиль по id
    @GetMapping("/{userId}")
    public String getUserProfile(@PathVariable Long userId, Model model) {
        User user = userService.getUserProfileById(userId);
        Long currentUserId = getCurrentUserId();
        User currentUser = userService.getUserById(currentUserId);
        if (user.getAvatar() != null) {
            String base64Avatar = Base64.getEncoder().encodeToString(user.getAvatar());
            model.addAttribute("base64Avatar", base64Avatar);
        }
        boolean areFriends = userService.areFriends(currentUserId, userId);
        List<Post> userPosts = postService.getPostsByAuthor(user);


        model.addAttribute("currentUser", currentUser);
        model.addAttribute("areFriends", areFriends);
        model.addAttribute("user", user);
        model.addAttribute("postCreationDto", new PostCreationDto());
        model.addAttribute("userPosts", userPosts);

        return "user-profile";
    }
//профиль пользователя
    @GetMapping
    public String getProfile(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        byte[] avatar = userService.getUserAvatar(user.getId());

        // Проверяем, есть ли у пользователя аватар, и добавляем его в модель
        if (avatar != null) {
            String encodedAvatar = Base64.getEncoder().encodeToString(avatar);
            model.addAttribute("avatarImage", "data:image/jpeg;base64," + encodedAvatar);
        }
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
                userService.updateAvatar(updatedUser.getId(), avatarFile);
            }

            return "redirect:/profile";
        } catch (IOException e) {
            log.error("Ошибка при загрузке аватара: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при загрузке аватара.");
            return "redirect:/profile?error=avatar_upload_failed";
        }
    }
//
//@PostMapping("/update")
//public String updateProfile(@ModelAttribute User updatedUser,
//                            @RequestParam("avatarFile") MultipartFile avatarFile,
//                            @RequestParam(required = false) String relationshipStatus,
//                            @RequestParam(required = false) Long partnerId) throws IOException {
//    // Если пользователь загрузил новый аватар, обновляем его
//    if (!avatarFile.isEmpty()) {
//        updatedUser.setAvatar(avatarFile.getBytes());
//    }
//
//    userService.updateUser(updatedUser, relationshipStatus, partnerId);
//    return "redirect:/profile";
//}




    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            return userDetails.getId();
        }
        throw new IllegalStateException("User not authenticated or CustomUserDetails not found");
    }
}