package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.Comment;
import com.example.lucky_social_network.model.Post;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserService userService;
    private final NotificationService notificationService;

    public Comment addComment(Long postId, Long userId, String content) {
        Post post = postService.getPostById(postId).orElseThrow(EntityNotFoundException::new);
        User user = userService.getUserById(userId);

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(user);
        comment.setContent(content);
        comment.setTimestamp(LocalDateTime.now());

        comment = commentRepository.save(comment);

        notificationService.createCommentNotification(post, comment);

        return comment;
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByTimestampDesc(postId);
    }

    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));
    }

    public Comment updateComment(Long commentId, String newContent) {
        Comment comment = getCommentById(commentId);
        comment.setContent(newContent);
        return commentRepository.save(comment);
    }

    public void deleteComment(Long commentId) {
        Comment comment = getCommentById(commentId);
        commentRepository.delete(comment);
    }
}