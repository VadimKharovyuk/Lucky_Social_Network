package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.Like;
import com.example.lucky_social_network.model.Post;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.LikeRepository;
import com.example.lucky_social_network.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;


    @Transactional
    public void toggleLike(Post post, User user) {
        Optional<Like> existingLike = likeRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {
            // Если лайк уже существует, удаляем его
            likeRepository.delete(existingLike.get());
            post.getLikes().remove(existingLike.get());
            post.setLikeCount(post.getLikeCount() - 1);
        } else {
            // Если лайка нет, создаем новый
            Like newLike = new Like();
            newLike.setUser(user);
            newLike.setPost(post);
            newLike.setTimestamp(LocalDateTime.now());
            likeRepository.save(newLike);
            post.getLikes().add(newLike);
            post.setLikeCount(post.getLikeCount() + 1);
        }

        postRepository.save(post);
    }

    public Long getLikeCount(Post post) {
        return post.getLikeCount();
    }

    public boolean hasUserLikedPost(Post post, User user) {
        return likeRepository.existsByPostAndUser(post, user);
    }
}
