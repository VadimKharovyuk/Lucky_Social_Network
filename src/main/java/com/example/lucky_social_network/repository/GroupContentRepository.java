package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.GroupPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupContentRepository extends JpaRepository<GroupPost, Long> {

    @EntityGraph(attributePaths = {"author", "group"})
    List<GroupPost> findByGroupId(Long groupId);

    @EntityGraph(attributePaths = {"author", "group"})
    List<GroupPost> findByAuthorId(Long authorId);

    @EntityGraph(attributePaths = {"author", "group"})
    List<GroupPost> findByGroupIdOrderByTimestampDesc(Long groupId);

    // Для подсчета не нужен EntityGraph
    long countByGroupId(Long groupId);

    @EntityGraph(attributePaths = {"author", "group"})
    @Query("SELECT gp FROM GroupPost gp " +
            "WHERE gp.group.id = :groupId AND gp.content LIKE %:searchText%")
    List<GroupPost> findPostsContainingText(@Param("groupId") Long groupId,
                                            @Param("searchText") String searchText);

    @EntityGraph(attributePaths = {"author", "group"})
    @Query("SELECT gp FROM GroupPost gp " +
            "WHERE gp.group.id = :groupId " +
            "ORDER BY gp.likesCount DESC")
    List<GroupPost> findMostLikedPost(@Param("groupId") Long groupId);

    // Добавим методы с пагинацией
    @EntityGraph(attributePaths = {"author", "group"})
    Page<GroupPost> findByGroupIdOrderByTimestampDesc(Long groupId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "group"})
    Page<GroupPost> findByAuthorIdOrderByTimestampDesc(Long authorId, Pageable pageable);

    // Переопределим базовые методы
    @Override
    @EntityGraph(attributePaths = {"author", "group"})
    Optional<GroupPost> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"author", "group"})
    List<GroupPost> findAll();
}
