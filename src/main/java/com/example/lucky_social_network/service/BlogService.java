// BlogService.java
package com.example.lucky_social_network.service;


import com.example.lucky_social_network.dto.BlogDTO;
import com.example.lucky_social_network.exception.BlogNotFoundException;
import com.example.lucky_social_network.maper.BlogMapper;
import com.example.lucky_social_network.model.BlogPost;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.BlogRepository;
import com.example.lucky_social_network.service.picService.ImgurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;
    private final BlogMapper blogMapper;
    private final ImgurService imgurService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Page<BlogDTO> getAllBlogs(BlogPost.BlogType type, Pageable pageable) {
        Page<BlogPost> posts = type == null ?
                blogRepository.findAllByOrderByCreatedAtDesc(pageable) :
                blogRepository.findAllByTypeOrderByCreatedAtDesc(type, pageable);

        return posts.map(blogMapper::toDTO);
    }

    // Изменим метод получения публичных постов
    public Page<BlogDTO> getPublishedBlogs(BlogPost.BlogType type, Pageable pageable) {
        Page<BlogPost> posts;
        if (type == null) {
            posts = blogRepository.findAllByPublishedTrue(pageable);
        } else {
            posts = blogRepository.findAllByPublishedTrueAndType(type, pageable);
        }
        return posts.map(blogMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public BlogDTO getBlogById(Long id) {
        return blogRepository.findById(id)
                .map(blogMapper::toDTO)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + id));
    }

    @Transactional
    public BlogDTO createBlog(BlogDTO dto, List<MultipartFile> images, Long authorId) {
        User author = userService.getUserById(authorId);

        BlogPost post = new BlogPost();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setType(BlogPost.BlogType.valueOf(dto.getType()));
        post.setAuthor(author);
        post.setPublished(false); // По умолчанию черновик

        // Загрузка изображений
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = images.stream()
                    .map(this::uploadImage)
                    .filter(url -> url != null)
                    .collect(Collectors.toList());
            post.setImageUrls(imageUrls);
        }

        BlogPost savedPost = blogRepository.save(post);
        log.info("Created new blog post with id: {}", savedPost.getId());
        return blogMapper.toDTO(savedPost);
    }

    @Transactional
    public BlogDTO updateBlog(Long id, BlogDTO dto, List<MultipartFile> newImages) {
        BlogPost post = blogRepository.findById(id)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + id));

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setType(BlogPost.BlogType.valueOf(dto.getType()));

        // Загрузка новых изображений
        if (newImages != null && !newImages.isEmpty()) {
            List<String> newImageUrls = newImages.stream()
                    .map(this::uploadImage)
                    .filter(url -> url != null)
                    .collect(Collectors.toList());

            // Добавляем новые изображения к существующим
            List<String> currentUrls = post.getImageUrls();
            currentUrls.addAll(newImageUrls);
            post.setImageUrls(currentUrls);
        }

        BlogPost updatedPost = blogRepository.save(post);
        log.info("Updated blog post with id: {}", updatedPost.getId());
        return blogMapper.toDTO(updatedPost);
    }

    @Transactional
    public void deleteBlog(Long id) {
        if (!blogRepository.existsById(id)) {
            throw new BlogNotFoundException("Blog not found with id: " + id);
        }
        blogRepository.deleteById(id);
        log.info("Deleted blog post with id: {}", id);
    }

    @Transactional
    public void publishBlog(Long id) {
        BlogPost post = blogRepository.findById(id)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + id));
        post.setPublished(true);
        blogRepository.save(post);
    }

    @Transactional
    public void unpublishBlog(Long id) {
        BlogPost post = blogRepository.findById(id)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + id));
        post.setPublished(false);
        blogRepository.save(post);
    }

    @Transactional
    public void incrementViewCount(Long id) {
        blogRepository.findById(id)
                .ifPresent(post -> {
                    post.incrementViewCount();
                    blogRepository.save(post);
                    log.debug("Incremented view count for blog id: {}", id);
                });
    }

    private String uploadImage(MultipartFile file) {
        try {
            return imgurService.uploadImage(file.getBytes());
        } catch (IOException e) {
            log.error("Failed to upload image", e);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public Page<BlogDTO> searchBlogs(String query, Pageable pageable) {
        return blogRepository.searchByTitleOrContent(query, pageable)
                .map(blogMapper::toDTO);
    }

    // Если нужно указывать количество
    @Transactional(readOnly = true)
    public List<BlogDTO> getLatestBlogs(int limit) {
        return blogRepository.findTopNByOrderByCreatedAtDesc(limit)
                .stream()
                .map(blogMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BlogDTO> getMostViewedBlogs(int limit) {
        return blogRepository.findTopNByOrderByViewCountDesc(limit).stream()
                .map(blogMapper::toDTO)
                .collect(Collectors.toList());
    }
}