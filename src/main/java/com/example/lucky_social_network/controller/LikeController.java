package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.Post;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.CustomUserDetails;
import com.example.lucky_social_network.service.LikeService;
import com.example.lucky_social_network.service.PostService;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
@Slf4j
@Controller
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    private final PostService postService;
    private final UserService userService;

    @PostMapping("/toggle")
    public String toggleLike(@RequestParam Long postId, @RequestParam String redirectUrl) {
        log.info("Received like toggle request for post: {}", postId);
        try {
            Long currentUserId = getCurrentUserId();
            User currentUser = userService.getUserById(currentUserId);
            log.info("Current user: {}", currentUser.getId());

            Optional<Post> postOptional = postService.getPostById(postId);
            if (postOptional.isEmpty()) {
                log.warn("Post not found: {}", postId);
                return "redirect:" + redirectUrl;
            }

            Post post = postOptional.get();
            likeService.toggleLike(post, currentUser);

            log.info("Like toggled. New like count: {}, Liked by user: {}",
                    post.getLikeCount(), likeService.hasUserLikedPost(post, currentUser));
        } catch (Exception e) {
            log.error("Error toggling like for post {}: {}", postId, e.getMessage(), e);
        }
        return "redirect:" + redirectUrl;
    }

    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getLikeCount(@RequestParam Long postId) {
        Optional<Post> postOptional = postService.getPostById(postId);
        Long likeCount = postOptional.map(likeService::getLikeCount).orElse(0L);

        Map<String, Long> response = new HashMap<>();
        response.put("likeCount", likeCount);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> getLikeStatus(@RequestParam Long postId) {
        Long currentUserId = getCurrentUserId();
        User currentUser = userService.getUserById(currentUserId);
        Optional<Post> postOptional = postService.getPostById(postId);

        boolean hasLiked = postOptional
                .map(post -> likeService.hasUserLikedPost(post, currentUser))
                .orElse(false);

        Map<String, Boolean> response = new HashMap<>();
        response.put("hasLiked", hasLiked);

        return ResponseEntity.ok(response);
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