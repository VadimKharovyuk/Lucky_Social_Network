package com.example.lucky_social_network.service;

import com.example.lucky_social_network.exception.ResourceNotFoundException;
import com.example.lucky_social_network.model.Group;
import com.example.lucky_social_network.model.GroupPost;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.GroupContentRepository;
import com.example.lucky_social_network.repository.GroupRepository;
import com.example.lucky_social_network.service.picService.ImgurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserService userService;
    private final ImgurService imgurService;
    private final GroupContentRepository groupContentRepository;


    @Transactional
    public Group updateGroup(Long groupId, String name, String description, byte[] photoData) {
        Group group = getGroupById(groupId);

        if (name != null && !name.trim().isEmpty()) {
            group.setName(name);
        }

        if (description != null) {
            group.setDescription(description);
        }

        if (photoData != null && photoData.length > 0) {
            String imageUrl = imgurService.uploadImage(photoData);
            if (imageUrl != null) {
                group.setImgurImageUrl(imageUrl);
            } else {
                log.error("Failed to upload image for group with id: {}", groupId);
                throw new RuntimeException("Failed to upload image to Imgur");
            }
        }

        Group updatedGroup = groupRepository.save(group);
        log.info("Updated group with id: {}", groupId);
        return updatedGroup;
    }

    public byte[] getGroupPhoto(Long groupId) {
        Group group = getGroupById(groupId);
        if (group.getImgurImageUrl() == null) {
            log.info("No photo found for group with id: {}", groupId);
            return null;
        }

        try {
            URL url = new URL(group.getImgurImageUrl());
            try (InputStream in = url.openStream()) {
                return in.readAllBytes();
            }
        } catch (IOException e) {
            log.error("Error fetching photo for group with id: {}", groupId, e);
            return null;
        }
    }

    @Transactional
    public Group createGroup(Group group, User owner) {

        if (owner == null) {
            log.info("Owner not provided, attempting to get current user");
            owner = userService.getCurrentUser();
        }
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
        log.info("Adding user {} to group {}", user.getUsername(), group.getName());
        if (group.getType() == Group.GroupType.INTERACTIVE) {
            if (!group.getMembers().contains(user)) {
                group.getMembers().add(user);
                group.setMembersCount(group.getMembersCount() + 1);
                groupRepository.save(group);
                log.info("User {} successfully added to group {}", user.getUsername(), group.getName());
            } else {
                log.info("User {} is already a member of group {}", user.getUsername(), group.getName());
            }
        } else {
            log.warn("Cannot add members to non-interactive group {}", group.getName());
            throw new IllegalStateException("Cannot add members to a non-interactive group");
        }
    }

    @Transactional
    public void subscribeToGroup(Group group, User user) {
        log.info("Subscribing user {} to group {}", user.getUsername(), group.getName());
        if (group.getType() == Group.GroupType.SUBSCRIPTION) {
            if (!group.getMembers().contains(user)) {
                group.getMembers().add(user);
                group.setMembersCount(group.getMembersCount() + 1);
                groupRepository.save(group);
                log.info("User {} successfully subscribed to group {}", user.getUsername(), group.getName());
            } else {
                log.info("User {} is already subscribed to group {}", user.getUsername(), group.getName());
            }
        } else {
            log.warn("Cannot subscribe to non-subscription group {}", group.getName());
            throw new IllegalStateException("Cannot subscribe to a non-subscription group");
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
    public GroupPost createPost(Long groupId, User author, String content, byte[] imageData) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

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

        if (imageData != null && imageData.length > 0) {
            try {
                String imageUrl = imgurService.uploadImage(imageData);
                if (imageUrl != null) {
                    post.setImgurImageUrl(imageUrl);
                    log.info("Image uploaded successfully for post. URL: {}", imageUrl);
                } else {
                    log.error("Failed to upload image for post");
                    throw new RuntimeException("Failed to upload image to Imgur");
                }
            } catch (Exception e) {
                log.error("Error uploading image to Imgur", e);
                throw new RuntimeException("Error uploading image to Imgur", e);
            }
        }

        GroupPost savedPost = groupContentRepository.save(post);

        group.getPosts().add(savedPost);
        group.setPostsCount(group.getPostsCount() + 1);
        groupRepository.save(group);

        return savedPost;
    }

//    @Transactional
//    public GroupPost createPost(Group group, User author, String content) {
//        if (group.getType() == Group.GroupType.SUBSCRIPTION && !author.equals(group.getOwner())) {
//            throw new IllegalStateException("Only the owner can post in a subscription group");
//        }
//
//        if (group.getType() == Group.GroupType.INTERACTIVE && !group.getMembers().contains(author)) {
//            throw new IllegalStateException("Only members can post in an interactive group");
//        }
//
//        GroupPost post = new GroupPost();
//        post.setGroup(group);
//        post.setAuthor(author);
//        post.setContent(content);
//        post.setTimestamp(LocalDateTime.now());
//
//        group.getPosts().add(post);
//        group.setPostsCount(group.getPostsCount() + 1);
//        groupRepository.save(group);
//
//        return post;
//    }

    @Transactional(readOnly = true)
    public boolean canUserPostInGroup(User user, Group group) {
        if (group.getType() == Group.GroupType.SUBSCRIPTION) {
            return user.equals(group.getOwner());
        } else if (group.getType() == Group.GroupType.INTERACTIVE) {
            return group.getMembers().contains(user);
        }
        return false;
    }


    @Transactional
    public void updateGroupAvatar(Group group, String avatarDropboxPath) {
        group.setImgurImageUrl(avatarDropboxPath);
        groupRepository.save(group);
    }

    @Transactional
    public void toggleGroupPrivacy(Group group) {
        group.setIsPrivate(!group.getIsPrivate());
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


    public Page<Group> getCurrentUserGroups(int page, int size, String sortBy, String sortDirection) {
        Long currentUserId = userService.getCurrentUserId();
        Sort sort = Sort.by(sortDirection.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return groupRepository.findByMembersId(currentUserId, pageable);
    }

    @Transactional(readOnly = true)
    public boolean isUserMemberOfGroup(Long userId, Long groupId) {
        return groupRepository.existsByIdAndMembersId(groupId, userId);
    }

    @Transactional(readOnly = true)
    public boolean isUserOwnerOfGroup(Long userId, Long groupId) {
        return groupRepository.existsByIdAndOwnerId(groupId, userId);
    }

}