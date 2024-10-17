package com.example.lucky_social_network.service;

import com.example.lucky_social_network.dto.PostCreationDto;
import com.example.lucky_social_network.model.Post;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;


    @Transactional
    public Post createPost(User author, PostCreationDto postDto) {
        Post post = new Post();
        post.setAuthor(author);
        post.setContent(postDto.getContent());
        post.setTimestamp(LocalDateTime.now());
        post.setLatitude(postDto.getLatitude());
        post.setLongitude(postDto.getLongitude());
        post.setLocationName(postDto.getLocationName());
        return postRepository.save(post);
    }
//    @Transactional
//    public Post createPost(User author, String content) {
//        Post post = new Post();
//        post.setAuthor(author);
//        post.setContent(content);
//        post.setTimestamp(LocalDateTime.now());
//        return postRepository.save(post);
//    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public List<Post> getAllPostsSortedByDateDesc() {
        return postRepository.findAllByOrderByTimestampDesc();
    }

    public List<Post> getPostsByAuthor(User author) {
        return postRepository.findByAuthorOrderByTimestampDesc(author);
    }

    @Transactional
    public Post updatePost(Long id, String newContent) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + id));
        post.setContent(newContent);
        return postRepository.save(post);
    }


    @Transactional
    public void deletePost(Long postId, Long userId) throws AccessDeniedException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to delete this post");
        }

        postRepository.delete(post);
    }


    @Transactional
    public void incrementLikeCount(Post post) {
        post.setLikeCount(post.getLikeCount() + 1);
        Post savedPost = postRepository.save(post);
        log.info("Incremented like count for post {}: {}", savedPost.getId(), savedPost.getLikeCount());
    }

    @Transactional
    public void decrementLikeCount(Post post) {
        if (post.getLikeCount() > 0) {
            post.setLikeCount(post.getLikeCount() - 1);
            Post savedPost = postRepository.save(post);
            log.info("Decremented like count for post {}: {}", savedPost.getId(), savedPost.getLikeCount());
        }

    }
}
