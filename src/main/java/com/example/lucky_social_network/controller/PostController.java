package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.PostCreationDto;
import com.example.lucky_social_network.model.Comment;
import com.example.lucky_social_network.model.Post;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {


    private final PostService postService;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final CommentService commentService;
    private final LikeService likeService;
    private final GroupService groupService;
    private final GroupContentService groupContentService;


    @GetMapping
    public String getAllPosts(Model model) {
        List<Post> posts = postService.getAllPostsSortedByDateDesc();
        model.addAttribute("posts", posts);
        User currentUser = userService.getCurrentUser();


        Long currentUserId = null;
        try {
            currentUserId = userService.getCurrentUserId();
        } catch (IllegalStateException e) {
            // Пользователь не аутентифицирован, оставляем currentUserId как null
        }
        model.addAttribute("currentUser", currentUser);

        return "posts/list";
    }


    @GetMapping("/{id}")
    public String getPost(@PathVariable Long id, Model model) {
        Optional<Post> postOptional = postService.getPostById(id);
        if (postOptional.isPresent()) {
            Post post = postOptional.get();
            User currentUser = userService.getCurrentUser();

            model.addAttribute("post", post);
            model.addAttribute("likeCount", likeService.getLikeCount(post));
            model.addAttribute("userLiked", likeService.hasUserLikedPost(post, currentUser));

            // Получаем комментарии к посту
            List<Comment> comments = commentService.getCommentsByPostId(id);
            model.addAttribute("comments", comments);

            // Добавляем пустой объект Comment для формы
            model.addAttribute("newComment", new Comment());

            // Добавляем текущего пользователя
            model.addAttribute("currentUserId", currentUser.getId());


            return "posts/view";
        } else {
            return "error/404";
        }
    }


    @PostMapping("/{id}/like")
    public String likePost(@PathVariable Long id, Model model) {
        Optional<Post> postOptional = postService.getPostById(id);
        if (postOptional.isPresent()) {
            Post post = postOptional.get();
            User currentUser = userService.getCurrentUser();

            likeService.toggleLike(post, currentUser);

            // Обновляем модель
            model.addAttribute("post", post);
            model.addAttribute("likeCount", likeService.getLikeCount(post));
            model.addAttribute("userLiked", likeService.hasUserLikedPost(post, currentUser));

            // Получаем комментарии к посту
            List<Comment> comments = commentService.getCommentsByPostId(id);
            model.addAttribute("comments", comments);

            // Добавляем пустой объект Comment для формы
            model.addAttribute("newComment", new Comment());

            // Добавляем текущего пользователя
            model.addAttribute("currentUserId", currentUser.getId());

            return "posts/view";
        } else {
            return "error/404";
        }
    }


    //посты юзера
    @GetMapping("/user/{userId}")
    public String getUserPosts(@PathVariable Long userId, Model model) {
        User user = userService.getUserById(userId);
        List<Post> userPosts = postService.getPostsByAuthor(user);

        // Получаем текущего пользователя
        User currentUser = userService.getCurrentUser();

        // Получаем информацию о лайках текущего пользователя
        Set<Long> likedPostIds = likeService.getLikedPostIdsByUserAndPosts(currentUser, userPosts);

        model.addAttribute("user", user);
        model.addAttribute("userPosts", userPosts);
        model.addAttribute("likedPostIds", likedPostIds);
        model.addAttribute("currentUser", currentUser);

        return "posts/user-posts";
    }


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




    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            return userDetails.getId();
        }
        throw new IllegalStateException("User not authenticated or CustomUserDetails not found");
    }
}