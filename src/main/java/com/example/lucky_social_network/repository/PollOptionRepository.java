package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Poll;
import com.example.lucky_social_network.model.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
    void deleteByPoll(Poll poll);

    @Modifying
    @Query("DELETE FROM PollOption o WHERE o.poll.group.id = :groupId")
    void deleteAllByPollGroupId(@Param("groupId") Long groupId);
}
