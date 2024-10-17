package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByType(Group.GroupType type);

    List<Group> findByOwnerId(Long ownerId);

    List<Group> findByIsPrivate(Boolean isPrivate);
}
