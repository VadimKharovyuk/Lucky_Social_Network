package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.Group;
import com.example.lucky_social_network.model.GroupPost;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.GroupContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class GroupContentService {

    private final GroupContentRepository groupContentRepository;
    private final GroupService groupService;


    @Transactional
    public GroupPost createPost(Long groupId, User author, String content, String imgurImageUrl) {
        Group group = groupService.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found");
        }

        GroupPost post = new GroupPost();
        post.setGroup(group);
        post.setAuthor(author);
        post.setContent(content);
        post.setImgurImageUrl(imgurImageUrl);
        post.setTimestamp(LocalDateTime.now());

        GroupPost savedPost = groupContentRepository.save(post);

        // Update group's post count
        group.setPostsCount(group.getPostsCount() + 1);
        groupService.save(group);

        return savedPost;
    }

    public List<GroupPost> getPostsByGroup(Long groupId) {
        return groupContentRepository.findByGroupId(groupId);
    }

    public List<GroupPost> getPostsByGroupSortedByDate(Long groupId) {
        return groupContentRepository.findByGroupIdOrderByTimestampDesc(groupId);
    }

    public List<GroupPost> getPostsByAuthor(Long authorId) {
        return groupContentRepository.findByAuthorId(authorId);
    }

    public GroupPost findById(Long postId) {
        Optional<GroupPost> post = groupContentRepository.findById(postId);
        return post.orElse(null);
    }

    @Transactional
    public void deletePost(Long postId) {
        GroupPost post = findById(postId);
        if (post != null) {
            Group group = post.getGroup();
            groupContentRepository.delete(post);

            // Update group's post count
            group.setPostsCount(group.getPostsCount() - 1);
            groupService.save(group);
        }
    }

    public boolean isAuthorOrGroupOwner(Long postId, User user) {
        GroupPost post = findById(postId);
        if (post == null) {
            return false;
        }
        return post.getAuthor().equals(user) || post.getGroup().getOwner().equals(user);
    }

    @Transactional
    public GroupPost updatePost(Long postId, String newContent, String newImgurImageUrl) {
        GroupPost post = findById(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post not found");
        }

        post.setContent(newContent);
        post.setImgurImageUrl(newImgurImageUrl);
        return groupContentRepository.save(post);
    }

    public void save(GroupPost originalPost) {
        groupContentRepository.save(originalPost);
    }


}