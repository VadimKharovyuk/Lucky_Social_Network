package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.PostCreationDto;
import com.example.lucky_social_network.model.Post;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.CustomUserDetails;
import com.example.lucky_social_network.service.PostService;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
@Slf4j
@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;



    @PostMapping("/create")
    public String createPost(@ModelAttribute PostCreationDto postDto,
                             @RequestParam("userId") Long userId,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.getUserById(userId);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пользователь не найден");
            return "redirect:/login";
        }
        try {
            postService.createPost(currentUser, postDto.getContent());
            redirectAttributes.addFlashAttribute("successMessage", "Пост успешно создан");
            return "redirect:/profile/" + currentUser.getId();
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for post creation", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Неверные данные для создания поста");
            return "redirect:/profile/" + currentUser.getId();
        } catch (Exception e) {
            log.error("Unexpected error while creating post", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Произошла ошибка при создании поста");
            return "redirect:/error";
        }
    }


    @GetMapping
    public String getAllPosts(Model model) {
        model.addAttribute("posts", postService.getAllPosts());
        return "posts/list";
    }

    @GetMapping("/{id}")
    public String getPost(@PathVariable Long id, Model model) {
        postService.getPostById(id).ifPresent(post -> model.addAttribute("post", post));
        return "posts/view";
    }

//    @PostMapping("/create")
//    public String createPost(@ModelAttribute Post post, @AuthenticationPrincipal User currentUser) {
//        postService.createPost(currentUser, post.getContent());
//        return "redirect:/profile/" + currentUser.getId();
//    }

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

    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return "redirect:/posts";
    }

    @PostMapping("/{id}/like")
    public String likePost(@PathVariable Long id) {
        postService.getPostById(id).ifPresent(postService::incrementLikeCount);
        return "redirect:/posts/" + id;
    }




    @GetMapping("/create")
    public String createPostForm(Model model) {
        model.addAttribute("post", new Post());
        return "posts/create";
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