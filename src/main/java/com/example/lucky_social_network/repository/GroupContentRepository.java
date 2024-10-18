package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.GroupPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupContentRepository extends JpaRepository<GroupPost, Long> {

    // Найти все посты для конкретной группы
    List<GroupPost> findByGroupId(Long groupId);

    // Найти все посты конкретного автора
    List<GroupPost> findByAuthorId(Long authorId);

    // Получаем посты группы, отсортированные по дате (самые новые первые)
    List<GroupPost> findByGroupIdOrderByTimestampDesc(Long groupId);

    // Посчитать количество постов в группе
    long countByGroupId(Long groupId);

    // Найти посты, содержащие определенный текст
    @Query("SELECT gp FROM GroupPost gp WHERE gp.group.id = :groupId AND gp.content LIKE %:searchText%")
    List<GroupPost> findPostsContainingText(@Param("groupId") Long groupId, @Param("searchText") String searchText);

    // Найти самый популярный пост в группе (с наибольшим количеством лайков)
    @Query("SELECT gp FROM GroupPost gp WHERE gp.group.id = :groupId ORDER BY gp.likesCount DESC")
    List<GroupPost> findMostLikedPost(@Param("groupId") Long groupId);
}
