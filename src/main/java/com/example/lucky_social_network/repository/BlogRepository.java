package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<BlogPost, Long> {

    // Исправленный метод поиска
    @Query("SELECT b FROM BlogPost b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(b.content) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<BlogPost> searchByTitleOrContent(@Param("query") String query, Pageable pageable);

    // Остальные методы
    Page<BlogPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<BlogPost> findAllByTypeOrderByCreatedAtDesc(BlogPost.BlogType type, Pageable pageable);

    // Исправленный метод для получения последних N записей
    @Query(value = "SELECT * FROM blog_posts ORDER BY created_at DESC LIMIT :limit",
            nativeQuery = true)
    List<BlogPost> findTopNByOrderByCreatedAtDesc(@Param("limit") int limit);

    // Метод для получения самых просматриваемых
    @Query(value = "SELECT * FROM blog_posts ORDER BY view_count DESC LIMIT :limit",
            nativeQuery = true)
    List<BlogPost> findTopNByOrderByViewCountDesc(@Param("limit") int limit);

    Page<BlogPost> findAllByPublishedTrue(Pageable pageable);

    Page<BlogPost> findAllByPublishedTrueAndType(BlogPost.BlogType type, Pageable pageable);

    Page<BlogPost> findAllByType(BlogPost.BlogType type, Pageable pageable);
}