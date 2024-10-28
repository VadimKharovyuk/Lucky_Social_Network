package com.example.lucky_social_network.maper;

import com.example.lucky_social_network.dto.BlogDTO;
import com.example.lucky_social_network.model.BlogPost;
import org.springframework.stereotype.Component;


@Component
public class BlogMapper {

    public BlogDTO toDTO(BlogPost post) {
        return BlogDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .type(post.getType().getDisplayName())
                .imageUrls(post.getImageUrls())
                .author(BlogDTO.UserDTO.builder()
                        .id(post.getAuthor().getId())
                        .username(post.getAuthor().getUsername())
                        .avatarUrl(post.getAuthor().getAvatarUrl())
                        .build())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .published(post.isPublished())
                .build();
    }
}
