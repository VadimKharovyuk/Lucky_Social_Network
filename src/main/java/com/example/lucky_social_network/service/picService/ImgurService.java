package com.example.lucky_social_network.service.picService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ImgurService {

    @Value("${imgur.client-id}")
    private String clientId;

    @Value("${imgur.access-token}")
    private String accessToken;

    private final String IMGUR_API_URL = "https://api.imgur.com/3/image";


    private final RestTemplate restTemplate = new RestTemplate();

    public String uploadImage(byte[] imageData) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Client-ID", clientId);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(imageData, headers);

        ResponseEntity<ImgurResponse> response = restTemplate.exchange(
                IMGUR_API_URL,
                HttpMethod.POST,
                requestEntity,
                ImgurResponse.class
        );

        if (response.getBody() != null && response.getBody().getData() != null) {
            return response.getBody().getData().getLink();
        }
        return null;
    }

    private static class ImgurResponse {
        private ImgurData data;

        public ImgurData getData() {
            return data;
        }

        public void setData(ImgurData data) {
            this.data = data;
        }
    }

    private static class ImgurData {
        private String link;

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }
}