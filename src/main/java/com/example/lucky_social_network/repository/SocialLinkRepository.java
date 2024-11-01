package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.SocialLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {
    // Найти ссылку по userId и платформе
    Optional<SocialLink> findByUserIdAndPlatform(Long userId, SocialLink.SocialPlatform platform);

    // Найти все ссылки пользователя
    List<SocialLink> findByUserId(Long userId);

    // Проверить существует ли ссылка с такой платформой у пользователя
    boolean existsByUserIdAndPlatform(Long userId, SocialLink.SocialPlatform platform);


}
