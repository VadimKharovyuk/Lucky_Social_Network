package com.example.lucky_social_network.service;

import com.example.lucky_social_network.dto.NotificationDto;
import com.example.lucky_social_network.model.Comment;
import com.example.lucky_social_network.model.Notification;
import com.example.lucky_social_network.model.Post;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.CommentRepository;
import com.example.lucky_social_network.repository.NotificationRepository;
import com.example.lucky_social_network.repository.PostRepository;
import com.example.lucky_social_network.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public void createCommentNotification(Post post, Comment comment) {
        if (!post.getAuthor().getId().equals(comment.getAuthor().getId())) {
            Notification notification = new Notification();
            notification.setUser(post.getAuthor());
            notification.setPost(post);
            notification.setComment(comment);
            notification.setMessage(comment.getAuthor().getUsername() + " commented on your post.");
            notificationRepository.save(notification);
        }
    }

    public List<Notification> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public long getUnreadNotificationCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    public void markAsRead(Long notificationId, Long userId) throws AccessDeniedException {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        // Проверяем, принадлежит ли уведомление текущему пользователю
        if (!notification.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to mark this notification as read");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }


    public List<NotificationDto> getUserNotificationsWithDetails(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private NotificationDto convertToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setRead(notification.isRead());

        if (notification.getComment().getId() != null) {
            Comment comment = commentRepository.findById(notification.getComment().getId())
                    .orElse(null);
            if (comment != null) {
                dto.setCommentContent(comment.getContent());
                dto.setCommentAuthorName(comment.getAuthor().getUsername());

                Post post = postRepository.findById(comment.getPost().getId())
                        .orElse(null);
                if (post != null) {
                    dto.setPostId(post.getId());
                    dto.setPostContent(post.getContent());
                }
            }
        }

        return dto;
    }
}