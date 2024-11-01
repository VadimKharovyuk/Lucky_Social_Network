package com.example.lucky_social_network.dto;

import com.example.lucky_social_network.model.SocialLink;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class SocialLinkCreateDTO {
    @NotNull(message = "ID пользователя не может быть пустым")
    private Long userId;

    @NotNull(message = "Платформа не может быть пустой")
    private SocialLink.SocialPlatform platform;

    @NotBlank(message = "URL не может быть пустым")
    @URL(message = "Неверный формат URL")
    private String url;
}
