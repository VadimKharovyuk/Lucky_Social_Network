package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Group;
import com.example.lucky_social_network.model.GroupJoinRequest;
import com.example.lucky_social_network.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequest, Long> {
    List<GroupJoinRequest> findAllByGroupAndStatus(Group group, GroupJoinRequest.RequestStatus status);

    List<GroupJoinRequest> findAllByUserAndStatus(User user, GroupJoinRequest.RequestStatus status);

    Optional<GroupJoinRequest> findByGroupAndUserAndStatus(Group group, User user, GroupJoinRequest.RequestStatus status);

    boolean existsByGroupAndUserAndStatus(Group group, User user, GroupJoinRequest.RequestStatus status);


    // Или альтернативный вариант с JPQL (предпочтительнее):
    @Query("SELECT COUNT(gr) FROM GroupJoinRequest gr WHERE gr.group.id = :groupId AND gr.status = :status")
    long countByGroupIdAndStatusJPQL(@Param("groupId") Long groupId,
                                     @Param("status") GroupJoinRequest.RequestStatus status);


    // Добавьте метод для отладки
    @Query(value = "SELECT * FROM group_join_requests WHERE group_id = :groupId",
            nativeQuery = true)
    List<GroupJoinRequest> findAllByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT gr FROM GroupJoinRequest gr " +
            "WHERE gr.group.id = :groupId AND gr.user.id = :userId " +
            "ORDER BY gr.createdAt DESC")
    Optional<GroupJoinRequest> findTopByGroupIdAndUserIdOrderByCreatedAtDesc(
            @Param("groupId") Long groupId,
            @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM GroupJoinRequest jr WHERE jr.group.id = :groupId")
    void deleteAllByGroupId(@Param("groupId") Long groupId);
}
