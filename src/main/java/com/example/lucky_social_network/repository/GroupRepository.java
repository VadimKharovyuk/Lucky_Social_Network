package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Group;
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
public interface GroupRepository extends JpaRepository<Group, Long> {

//    @EntityGraph(attributePaths = {"members", "owner"})
//    List<Group> findByType(Group.GroupType type);
//
//    @EntityGraph(attributePaths = {"members", "owner"})
//    List<Group> findByOwnerId(Long ownerId);
//
//    @EntityGraph(attributePaths = {"members", "owner"})
//    List<Group> findByIsPrivate(Boolean isPrivate);

    @EntityGraph(attributePaths = {"members", "owner"})
    Page<Group> findByMembersId(Long userId, Pageable pageable);

    // Для проверок членства используем оптимизированные JPQL запросы
    @Query("SELECT COUNT(g) > 0 FROM Group g JOIN g.members m WHERE g.id = :groupId AND m.id = :userId")
    boolean existsByIdAndMembersId(Long groupId, Long userId);

    @Query("SELECT COUNT(g) > 0 FROM Group g WHERE g.id = :groupId AND g.owner.id = :userId")
    boolean existsByIdAndOwnerId(Long groupId, Long userId);

    // Добавим метод для проверки возможности постинга
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Group g " +
            "LEFT JOIN g.members m " +
            "WHERE g.id = :groupId AND " +
            "((g.type = 'SUBSCRIPTION' AND g.owner.id = :userId) OR " +
            "(g.type = 'INTERACTIVE' AND m.id = :userId))")
    boolean canUserPostInGroup(@Param("groupId") Long groupId, @Param("userId") Long userId);

    // Переопределим базовые методы
    @Override
    @EntityGraph(attributePaths = {"members", "owner"})
    Optional<Group> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"members", "owner"})
    List<Group> findAll();

    // Добавим поиск по названию
    @EntityGraph(attributePaths = {"members", "owner"})
    Page<Group> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Поиск групп, где пользователь является владельцем или участником
    @Query("SELECT DISTINCT g FROM Group g " +
            "LEFT JOIN g.members m " +
            "WHERE g.owner.id = :userId OR m.id = :userId")
    @EntityGraph(attributePaths = {"members", "owner"})
    Page<Group> findUserGroups(@Param("userId") Long userId, Pageable pageable);
}
