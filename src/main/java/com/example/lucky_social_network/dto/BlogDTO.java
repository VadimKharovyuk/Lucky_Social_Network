package com.example.lucky_social_network.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlogDTO {
    private Long id;


    @NotBlank(message = "Заголовок обязателен")
    @Size(min = 3, max = 200, message = "Заголовок должен быть от 3 до 200 символов")
    private String title;

    @NotBlank(message = "Содержание обязательно")
    @Size(min = 10, message = "Содержание должно быть не менее 10 символов")
    private String content;

    @NotBlank(message = "Тип поста обязателен")
    private String type;

    private List<String> imageUrls;
    private UserDTO author;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean published = false; // По умолчанию false

    @Data
    @Builder
    public static class UserDTO {
        private Long id;
        private String username;
        private String avatarUrl;
    }
}
