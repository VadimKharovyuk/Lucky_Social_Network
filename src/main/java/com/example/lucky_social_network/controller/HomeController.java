package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.Post;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.CustomUserDetails;
import com.example.lucky_social_network.service.PostService;
import com.example.lucky_social_network.service.SubscriptionService;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {
    private final UserService userService;

    private final SubscriptionService subscriptionService;


    @GetMapping
    public String home(Model model) {
        List<User> userList= userService.getAllUsers();
        model.addAttribute("userList", userList);
        return "homePage";

    }

    @GetMapping("/home")
    public String homePage(Model model, @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {
        try {
            Long currentUserId = getCurrentUserId();
            log.debug("Fetching home page for user ID: {}", currentUserId);

            User currentUser = userService.getUserProfileById(currentUserId);
            log.debug("Current user: {}", currentUser.getUsername());

            Page<Post> newsFeed = subscriptionService.getNewsFeed(currentUserId, PageRequest.of(page, size));
            log.debug("Fetched {} posts for news feed", newsFeed.getTotalElements());

            model.addAttribute("currentUser", currentUser);
            model.addAttribute("newsFeed", newsFeed);

            return "home";
        } catch (Exception e) {
            log.error("Error occurred while fetching home page", e);
            model.addAttribute("errorMessage", "An error occurred while loading the home page. Please try again later.");
            return "error";
        }
    }

    @PostMapping("/subscribe/{followeeId}")
    public String subscribe(@PathVariable Long followeeId) {
        Long currentUserId = getCurrentUserId();
        subscriptionService.subscribe(currentUserId, followeeId);
        return "redirect:/home";
    }

    @PostMapping("/unsubscribe/{followeeId}")
    public String unsubscribe(@PathVariable Long followeeId) {
        Long currentUserId = getCurrentUserId();
        subscriptionService.unsubscribe(currentUserId, followeeId);
        return "redirect:/home";
    }

    @GetMapping("/profile-list")
    public String profileList(Model model) {
        List<User> userList= userService.getAllUsers();
        model.addAttribute("userList", userList);
        return "profileList";
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
