package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.Like;
import com.example.lucky_social_network.model.Post;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.LikeRepository;
import com.example.lucky_social_network.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final  PostService postService;


    @Transactional
    public void toggleLike(Post post, User user) {
        Optional<Like> existingLike = likeRepository.findByPostAndUser(post, user);
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            postService.decrementLikeCount(post);
        } else {
            Like newLike = new Like();
            newLike.setPost(post);
            newLike.setUser(user);
            newLike.setTimestamp(LocalDateTime.now());
            likeRepository.save(newLike);
            postService.incrementLikeCount(post);
        }
    }

    public Long getLikeCount(Post post) {
        return post.getLikeCount();
    }

    public boolean hasUserLikedPost(Post post, User user) {
        return likeRepository.existsByPostAndUser(post, user);
    }

    public Set<Long> getLikedPostIdsByUserAndPosts(User user, List<Post> posts) {
        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
        return likeRepository.findByUserAndPostIdIn(user, postIds).stream()
                .map(like -> like.getPost().getId())
                .collect(Collectors.toSet());
    }
}
