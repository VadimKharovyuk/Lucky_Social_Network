package com.example.lucky_social_network.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Long id;

    private Long commentAuthorId;
    private String message;
    private LocalDateTime createdAt;
    private boolean isRead;
    private String commentContent;
    private String commentAuthorName;
    private Long postId;
    private String postContent;

}