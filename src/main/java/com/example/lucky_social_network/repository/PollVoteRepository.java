package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Poll;
import com.example.lucky_social_network.model.PollVote;
import com.example.lucky_social_network.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    boolean existsByPollAndUser(Poll poll, User user);

    long countByPoll(Poll poll);

    @Query("SELECT COUNT(DISTINCT v.user) FROM PollVote v WHERE v.poll = :poll")
    long countDistinctUsersByPoll(Poll poll);

    void deleteByPoll(Poll poll);


    @Modifying
    @Query("DELETE FROM PollVote v WHERE v.poll.group.id = :groupId")
    void deleteAllByPollGroupId(@Param("groupId") Long groupId);


    void deleteAllByPollIdIn(@Param("pollIds") List<Long> pollIds);
}


