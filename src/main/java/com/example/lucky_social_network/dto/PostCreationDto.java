package com.example.lucky_social_network.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PostCreationDto {
    private String content;
    private Double latitude;
    private Double longitude;
    private String locationName;

    private MultipartFile image;
}
