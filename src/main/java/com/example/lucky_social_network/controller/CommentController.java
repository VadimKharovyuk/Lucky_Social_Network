package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.Comment;
import com.example.lucky_social_network.service.CommentService;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final UserService userService;


    @PostMapping("/delete/{commentId}")
    public String deleteComment(@PathVariable Long commentId) {
        Comment comment = commentService.getCommentById(commentId);
        Long postId = comment.getPost().getId();

        // Проверяем, является ли текущий пользователь автором комментария
        Long currentUserId = userService.getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(comment.getAuthor().getId())) {
            commentService.deleteComment(commentId);
        } else {
            // Можно добавить сообщение об ошибке или перенаправление
            return "redirect:/error";
        }

        return "redirect:/posts/" + postId;
    }


    @PostMapping("/add")
    public String addComment(@ModelAttribute("newComment") Comment newComment, @RequestParam Long postId, @RequestParam Long userId) {
        commentService.addComment(postId, userId, newComment.getContent());
        return "redirect:/posts";
    }

    @GetMapping("/edit/{commentId}")
    public String showEditForm(@PathVariable Long commentId, Model model) {
        Comment comment = commentService.getCommentById(commentId);
        model.addAttribute("comment", comment);
        return "comment/editComment";
    }

    @PostMapping("/edit/{commentId}")
    public String updateComment(@PathVariable Long commentId, @RequestParam String content) {
        Comment updatedComment = commentService.updateComment(commentId, content);
        return "redirect:/posts/" + updatedComment.getPost().getId();
    }




}