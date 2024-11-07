package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Group;
import com.example.lucky_social_network.model.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {
    List<Poll> findAllByGroupOrderByCreatedAtDesc(Group group);

    @Modifying
    @Query("DELETE FROM Poll p WHERE p.group.id = :groupId")
    void deleteAllByGroupId(@Param("groupId") Long groupId);


    List<Poll> findByGroupId(Long groupId);

    void deleteByGroupId(Long groupId);

}
