package com.example.lucky_social_network;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableRabbit
public class LuckySocialNetworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuckySocialNetworkApplication.class, args);
    }


    //настройки добавить навигацию
    //профиль добавить <adide>

    //docker buildx build --platform linux/amd64 -t lucky1911/lucky_social_network:latest . --push
    //push Docker Hub

}
