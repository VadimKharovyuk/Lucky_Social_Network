package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.GroupPost;
import com.example.lucky_social_network.repository.GroupContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service

public class GroupContentService {

    private final GroupContentRepository groupContentRepository;
    private final ActivityPublisher activityPublisher;


    public GroupPost findById(Long postId) {
        Optional<GroupPost> post = groupContentRepository.findById(postId);
        return post.orElse(null);
    }

    public void save(GroupPost originalPost) {
        groupContentRepository.save(originalPost);
    }


    public void deletePostById(Long postId) {
        groupContentRepository.deleteById(postId);
    }
}