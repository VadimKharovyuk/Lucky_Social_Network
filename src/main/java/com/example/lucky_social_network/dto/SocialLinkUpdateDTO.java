package com.example.lucky_social_network.dto;

import com.example.lucky_social_network.model.SocialLink;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class SocialLinkUpdateDTO {
    @NotNull
    private Long id;

    @URL
    private String url;

    private SocialLink.SocialPlatform platform;
}
