package com.example.lucky_social_network;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LuckySocialNetworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuckySocialNetworkApplication.class, args);
    }
//добавить новости  где подпищики смогут видеть посты других пользоватей
}
