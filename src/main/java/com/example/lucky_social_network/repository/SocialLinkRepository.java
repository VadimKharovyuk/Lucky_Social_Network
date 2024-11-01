package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.SocialLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {
    // Найти ссылку по userId и платформе
    Optional<SocialLink> findByUserIdAndPlatform(Long userId, SocialLink.SocialPlatform platform);
    
    @Query("SELECT sl FROM SocialLink sl WHERE sl.user.id = :userId")
    List<SocialLink> findByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(sl) > 0 FROM SocialLink sl " +
            "WHERE sl.user.id = :userId AND sl.platform = :platform")
    boolean existsByUserIdAndPlatform(@Param("userId") Long userId,
                                      @Param("platform") SocialLink.SocialPlatform platform);


}
