package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Group;
import com.example.lucky_social_network.model.GroupMembership;
import com.example.lucky_social_network.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {
    Optional<GroupMembership> findByGroupAndUser(Group group, User user);

    List<GroupMembership> findByUserId(Long userId);

    @Query("SELECT gm FROM GroupMembership gm " +
            "WHERE gm.user.id = :userId " +
            "AND gm.role IN :roles")
    List<GroupMembership> findByUserIdAndRoleIn(
            @Param("userId") Long userId,
            @Param("roles") Collection<GroupMembership.GroupRole> roles
    );

    @Query("SELECT gm FROM GroupMembership gm " +
            "WHERE gm.user.id = :userId " +
            "AND gm.group.id = :groupId " +
            "AND gm.role IN :roles")
    Optional<GroupMembership> findByUserAndGroupAndRoleIn(
            @Param("userId") Long userId,
            @Param("groupId") Long groupId,
            @Param("roles") Collection<GroupMembership.GroupRole> roles
    );
}
