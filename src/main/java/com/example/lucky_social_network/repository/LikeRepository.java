package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Like;
import com.example.lucky_social_network.model.Post;
import com.example.lucky_social_network.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByPostAndUser(Post post, User user);
    boolean existsByPostAndUser(Post post, User user) ;


    List<Like> findByUserAndPostIdIn(User user, List<Long> postIds);

}
