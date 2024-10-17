package com.example.lucky_social_network.service;

import com.example.lucky_social_network.exception.ResourceNotFoundException;
import com.example.lucky_social_network.model.Group;
import com.example.lucky_social_network.model.GroupPost;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class GroupService {

    private final GroupRepository groupRepository;

    private final UserService userService;

    @Transactional
    public Group createGroup(Group group, User owner) {
        log.info("Creating group: {}", group.getName());

        if (owner == null) {
            log.info("Owner not provided, attempting to get current user");
            owner = userService.getCurrentUser();
        }

        log.info("Owner: {}", owner.getUsername());

        group.setOwner(owner);
        group.setCreatedAt(LocalDateTime.now());
        group.setMembersCount(1L);
        group.getMembers().add(owner);

        Group savedGroup = groupRepository.save(group);
        log.info("Group created successfully with id: {}", savedGroup.getId());
        return savedGroup;
    }

    @Transactional
    public void addMember(Group group, User user) {
        if (group.getType() == Group.GroupType.INTERACTIVE) {
            group.getMembers().add(user);
            group.setMembersCount(group.getMembersCount() + 1);
            groupRepository.save(group);
        } else {
            throw new IllegalStateException("Cannot add members to a subscription group");
        }
    }

    @Transactional
    public void removeMember(Group group, User user) {
        if (group.getMembers().remove(user)) {
            group.setMembersCount(group.getMembersCount() - 1);
            groupRepository.save(group);
        }
    }

    @Transactional
    public GroupPost createPost(Group group, User author, String content) {
        if (group.getType() == Group.GroupType.SUBSCRIPTION && !author.equals(group.getOwner())) {
            throw new IllegalStateException("Only the owner can post in a subscription group");
        }

        if (group.getType() == Group.GroupType.INTERACTIVE && !group.getMembers().contains(author)) {
            throw new IllegalStateException("Only members can post in an interactive group");
        }

        GroupPost post = new GroupPost();
        post.setGroup(group);
        post.setAuthor(author);
        post.setContent(content);
        post.setTimestamp(LocalDateTime.now());

        group.getPosts().add(post);
        group.setPostsCount(group.getPostsCount() + 1);
        groupRepository.save(group);

        return post;
    }

    @Transactional
    public void updateGroupAvatar(Group group, String avatarDropboxPath) {
        group.setAvatarDropboxPath(avatarDropboxPath);
        groupRepository.save(group);
    }

    @Transactional
    public void toggleGroupPrivacy(Group group) {
        group.setIsPrivate(!group.getIsPrivate());
        groupRepository.save(group);
    }

    // Метод для подписки на группу (только для SUBSCRIPTION типа)
    @Transactional
    public void subscribeToGroup(Group group, User user) {
        if (group.getType() != Group.GroupType.SUBSCRIPTION) {
            throw new IllegalStateException("Can only subscribe to SUBSCRIPTION type groups");
        }
        group.getMembers().add(user);
        group.setMembersCount(group.getMembersCount() + 1);
        groupRepository.save(group);
    }

    // отписки от группы (только для SUBSCRIPTION типа)
    @Transactional
    public void unsubscribeFromGroup(Group group, User user) {
        if (group.getType() != Group.GroupType.SUBSCRIPTION) {
            throw new IllegalStateException("Can only unsubscribe from SUBSCRIPTION type groups");
        }
        if (group.getMembers().remove(user)) {
            group.setMembersCount(group.getMembersCount() - 1);
            groupRepository.save(group);
        }
    }

    // отписки от группы (только для INTERACTIVE типа)
    @Transactional
    public void leaveGroup(Group group, User user) {
        if (group.getType() != Group.GroupType.INTERACTIVE) {
            throw new IllegalStateException("Can only leave INTERACTIVE type groups");
        }
        if (user.equals(group.getOwner())) {
            throw new IllegalStateException("The owner cannot leave the group");
        }
        if (group.getMembers().remove(user)) {
            group.setMembersCount(group.getMembersCount() - 1);
            groupRepository.save(group);
        }
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public Group getGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
    }
    // Другие методы...
}