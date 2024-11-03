package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Post;
import com.example.lucky_social_network.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"author"})
    List<Post> findByAuthorOrderByTimestampDesc(User author);

    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT p FROM Post p ORDER BY p.timestamp DESC")
    List<Post> findTopNOrderByTimestampDesc(Pageable pageable);

    default List<Post> findTopNOrderByTimestampDesc(int limit) {
        return findTopNOrderByTimestampDesc(PageRequest.of(0, limit));
    }

    @EntityGraph(attributePaths = {"author"})
    Page<Post> findByAuthorIdInOrderByTimestampDesc(List<Long> authorIds, Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    List<Post> findAllByOrderByTimestampDesc();

   
    @Override
    @EntityGraph(attributePaths = {"author"})
    List<Post> findAll();

    @Override
    @EntityGraph(attributePaths = {"author"})
    Optional<Post> findById(Long id);
}