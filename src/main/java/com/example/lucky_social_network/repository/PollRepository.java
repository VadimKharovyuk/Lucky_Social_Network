package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Group;
import com.example.lucky_social_network.model.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {
    List<Poll> findAllByGroupOrderByCreatedAtDesc(Group group);

}
