package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.PostCreationDto;
import com.example.lucky_social_network.model.Comment;
import com.example.lucky_social_network.model.Post;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.CustomUserDetails;
import com.example.lucky_social_network.service.PostService;
import com.example.lucky_social_network.service.SubscriptionService;
import com.example.lucky_social_network.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.nio.file.AccessDeniedException;
import java.util.List;
@Slf4j
@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final SubscriptionService subscriptionService;

    @PostMapping("/create")
    public String createPost(@ModelAttribute PostCreationDto postDto,
                             @RequestParam("userId") Long userId) {
        User currentUser = userService.getUserById(userId);
        postService.createPost(currentUser, postDto);
        return "redirect:/profile/" + userId;
    }


    @PostMapping("/delete/{postId}")
    public String deletePost(@PathVariable Long postId,
                             HttpServletRequest request) throws AccessDeniedException {
        Long currentUserId = getCurrentUserId();
        postService.deletePost(postId, currentUserId);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @PostMapping("/subscribe/{followeeId}")
    public ResponseEntity<?> subscribe(@PathVariable Long followeeId) {
        Long currentUserId = getCurrentUserId();
        subscriptionService.subscribe(currentUserId, followeeId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unsubscribe/{followeeId}")
    public ResponseEntity<?> unsubscribe(@PathVariable Long followeeId) {
        Long currentUserId = getCurrentUserId();
        subscriptionService.unsubscribe(currentUserId, followeeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/news-feed")
    public ResponseEntity<Page<Post>> getNewsFeed(@PageableDefault(size = 20) Pageable pageable) {
        Long currentUserId = getCurrentUserId();
        Page<Post> newsFeed = subscriptionService.getNewsFeed(currentUserId, (org.springframework.data.domain.Pageable) pageable);
        return ResponseEntity.ok(newsFeed);
    }








    @GetMapping
    public String getAllPosts(Model model) {
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);

        // Добавляем пустой объект Comment для формы
        model.addAttribute("newComment", new Comment());

        // Получаем ID текущего пользователя, если он аутентифицирован
        Long currentUserId = null;
        try {
            currentUserId = userService.getCurrentUserId();
        } catch (IllegalStateException e) {
            // Пользователь не аутентифицирован, оставляем currentUserId как null
        }
        model.addAttribute("currentUserId", currentUserId);

        return "posts/list";
    }

    @GetMapping("/{id}")
    public String getPost(@PathVariable Long id, Model model) {
        postService.getPostById(id).ifPresent(post -> model.addAttribute("post", post));
        return "posts/view";
    }


    @GetMapping("/user/{userId}")
    public String getUserPosts(@PathVariable Long userId, Model model) {
        User user = userService.getUserById(userId);
        List<Post> userPosts = postService.getPostsByAuthor(user);
        model.addAttribute("userPosts", userPosts);
        return "posts/user-posts";
    }



    @GetMapping("/{id}/edit")
    public String editPostForm(@PathVariable Long id, Model model) {
        postService.getPostById(id).ifPresent(post -> model.addAttribute("post", post));
        return "posts/edit";
    }

    @PostMapping("/{id}/edit")
    public String editPost(@PathVariable Long id, @ModelAttribute Post post) {
        postService.updatePost(id, post.getContent());
        return "redirect:/posts/" + id;
    }



    @PostMapping("/{id}/like")
    public String likePost(@PathVariable Long id) {
        postService.getPostById(id).ifPresent(postService::incrementLikeCount);
        return "redirect:/posts/" + id;
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