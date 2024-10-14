package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Post;
import com.example.lucky_social_network.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorOrderByTimestampDesc(User author);

    @Query("SELECT p FROM Post p ORDER BY p.timestamp DESC")
    List<Post> findTopNOrderByTimestampDesc(Pageable pageable);

    default List<Post> findTopNOrderByTimestampDesc(int limit) {
        return findTopNOrderByTimestampDesc(PageRequest.of(0, limit));
    }

    Page<Post> findByAuthorIdInOrderByTimestampDesc(List<Long> authorIds, Pageable pageable);
}