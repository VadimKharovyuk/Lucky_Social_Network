package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Poll;
import com.example.lucky_social_network.model.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
    void deleteByPoll(Poll poll);
    
}
