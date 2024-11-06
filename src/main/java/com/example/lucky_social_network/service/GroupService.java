package com.example.lucky_social_network.service;

import com.example.lucky_social_network.exception.ResourceNotFoundException;
import com.example.lucky_social_network.model.*;
import com.example.lucky_social_network.repository.GroupContentRepository;
import com.example.lucky_social_network.repository.GroupJoinRequestRepository;
import com.example.lucky_social_network.repository.GroupRepository;
import com.example.lucky_social_network.service.picService.ImgurService;
import lombok.Builder;
import lombok.Data;
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
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserService userService;
    private final ImgurService imgurService;
    private final GroupContentRepository groupContentRepository;
    private final ActivityPublisher activityPublisher;
    private final GroupJoinRequestServiceImpl joinRequestService;
    private final GroupJoinRequestRepository groupJoinRequestRepository;


    public boolean canUserPostInGroup(User user, Group group) {
        return groupRepository.canUserPostInGroup(group.getId(), user.getId());
    }

    public boolean isMember(Long groupId, Long userId) {
        return groupRepository.existsByIdAndMembersId(groupId, userId);
    }

    // Или если у вас другая реализация, добавьте логирование
    public boolean isOwner(Long userId, Long groupId) {
        Group group = getGroupById(groupId);
        boolean isOwner = group.getOwner().getId().equals(userId);
        log.info("User {} {} owner of group {}. Owner ID: {}",
                userId,
                isOwner ? "is" : "is not",
                groupId,
                group.getOwner().getId());
        return isOwner;
    }

    public Group getGroupById(Long id) {
        activityPublisher.publishGroupActivity(id, GroupActivityEvent.GroupActivityType.GROUP_UPDATED);
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
    }


    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }


    @Transactional
    public Group createGroup(Group group, User owner) {
        if (owner == null) {
            log.info("Owner not provided, attempting to get current user");
            owner = userService.getCurrentUser();
        }

        // Устанавливаем настройки приватности в зависимости от типа видимости
        switch (group.getVisibility()) {
            case PRIVATE:
            case RESTRICTED:
                group.setRequiresJoinApproval(true);
                break;
            case PUBLIC:
                group.setRequiresJoinApproval(false);
                break;
        }

        group.setOwner(owner);
        group.setCreatedAt(LocalDateTime.now());
        group.setMembersCount(1L);
        group.getMembers().add(owner);

        Group savedGroup = groupRepository.save(group);
        log.info("Group created successfully with id: {} and visibility: {}",
                savedGroup.getId(), savedGroup.getVisibility());
        return savedGroup;
    }


    public boolean isUserMemberOfGroup(Long userId, Long groupId) {
        return groupRepository.existsByIdAndMembersId(groupId, userId);
    }


    public Page<Group> getCurrentUserGroups(int page, int size, String sortBy, String sortDirection, String type) {
        Long currentUserId = userService.getCurrentUserId();
        Sort sort = Sort.by(sortDirection.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return groupRepository.findByMembersId(currentUserId, pageable);
    }


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

        // Публикуем событие об обновлении группы
        activityPublisher.publishGroupActivity(groupId, GroupActivityEvent.GroupActivityType.GROUP_UPDATED);

        return updatedGroup;
    }
    @Transactional
    public Group deleteGroupById(Long groupId) {
        Optional<Group> groupOptional = groupRepository.findById(groupId);

        if (groupOptional.isPresent()) {
            Group group = groupOptional.get();
            group.getMembers().clear();
            group.getPosts().clear();
            groupRepository.delete(group);
            return group;
        } else {
            throw new RuntimeException("Group not found with id: " + groupId);
        }
    }


    @Transactional
    public void addMember(Group group, User user) {
        if (group.getType() != Group.GroupType.INTERACTIVE) {
            throw new IllegalStateException("Cannot add members to a non-interactive group");
        }

        Group freshGroup = groupRepository.findById(group.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        if (!freshGroup.getMembers().contains(user)) {
            // Проверяем необходимость одобрения
            if (needsJoinRequest(freshGroup)) {
                // Если нет активной заявки, создаем её
                if (!hasActiveJoinRequest(freshGroup.getId(), user)) {
                    throw new IllegalStateException("Join request required for this group");
                }
                // Проверяем статус заявки
                GroupJoinRequest request = groupJoinRequestRepository
                        .findByGroupAndUserAndStatus(freshGroup, user, GroupJoinRequest.RequestStatus.APPROVED)
                        .orElseThrow(() -> new IllegalStateException("Join request not approved"));
            }

            freshGroup.getMembers().add(user);
            freshGroup.setMembersCount(freshGroup.getMembersCount() + 1);
            groupRepository.save(freshGroup);
            log.info("User {} successfully added to group {}", user.getUsername(), freshGroup.getName());
        }
    }

    // GroupServiceImpl.java - добавляем новый метод для проверки существующего запроса
    @Transactional(readOnly = true)
    public boolean hasActiveJoinRequest(Long groupId, User user) {
        return groupJoinRequestRepository.existsByGroupAndUserAndStatus(
                Group.builder().id(groupId).build(),
                user,
                GroupJoinRequest.RequestStatus.PENDING
        );
    }

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
    public void subscribeToGroup(Group group, User user) {
        log.info("Subscribing user {} to group {}", user.getUsername(), group.getName());
        if (group.getType() == Group.GroupType.SUBSCRIPTION) {
            if (!group.getMembers().contains(user)) {
                group.getMembers().add(user);
                group.setMembersCount(group.getMembersCount() + 1);
                groupRepository.save(group);
                activityPublisher.publishActivity(user.getId(), UserActivityEvent.ActivityType.LIKE_ADDED);
                log.info("User {} successfully subscribed to group {}", user.getUsername(), group.getName());
            } else {
                log.info("User {} is already subscribed to group {}", user.getUsername(), group.getName());
            }
        } else {
            log.warn("Cannot subscribe to non-subscription group {}", group.getName());
            throw new IllegalStateException("Cannot subscribe to a non-subscription group");
        }
    }

    //
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
        activityPublisher.publishActivity(author.getId(), UserActivityEvent.ActivityType.LIKE_ADDED);

        group.getPosts().add(savedPost);
        group.setPostsCount(group.getPostsCount() + 1);
        groupRepository.save(group);

        return savedPost;
    }

    //
//
//    // отписки от группы (только для SUBSCRIPTION типа)
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

    @Transactional(readOnly = true)
    public boolean canAccessGroup(User user, Group group) {
        // Владелец всегда имеет доступ
        if (isOwner(user.getId(), group.getId())) {
            return true;
        }

        // Участник группы всегда имеет доступ
        if (isUserMemberOfGroup(user.getId(), group.getId())) {
            return true;
        }

        // Проверка доступа на основе типа видимости
        switch (group.getVisibility()) {
            case PUBLIC:
                return true;
            case RESTRICTED:
                // Проверяем наличие одобренной заявки
                return groupJoinRequestRepository
                        .findByGroupAndUserAndStatus(group, user, GroupJoinRequest.RequestStatus.APPROVED)
                        .isPresent();
            case PRIVATE:
                return false;
            default:
                return false;
        }
    }

    @Transactional(readOnly = true)
    public boolean needsJoinRequest(Group group) {
        return group.getVisibility() != Group.VisibilityType.PUBLIC || group.getRequiresJoinApproval();
    }

    @Transactional(readOnly = true)
    public GroupAccessInfo getGroupAccessInfo(Long groupId, User currentUser) {
        Group group = getGroupById(groupId);
        boolean isMember = isUserMemberOfGroup(currentUser.getId(), groupId);
        boolean isOwner = isOwner(currentUser.getId(), groupId);

        // Получаем статус заявки, если пользователь не участник и не владелец
        GroupJoinRequest.RequestStatus joinRequestStatus = null;
        if (!isMember && !isOwner) {
            joinRequestStatus = groupJoinRequestRepository
                    .findTopByGroupIdAndUserIdOrderByCreatedAtDesc(groupId, currentUser.getId())
                    .map(GroupJoinRequest::getStatus)
                    .orElse(null);
        }

        // Определяем, может ли пользователь просматривать контент
        boolean canAccess = calculateCanAccess(group, currentUser, isMember, isOwner, joinRequestStatus);

        log.info("Access info for group {}: isMember={}, isOwner={}, canAccess={}, visibility={}, requestStatus={}",
                groupId, isMember, isOwner, canAccess, group.getVisibility(), joinRequestStatus);

        return GroupAccessInfo.builder()
                .canAccess(canAccess)
                .isMember(isMember)
                .isOwner(isOwner)
                .joinRequestStatus(joinRequestStatus)
                .visibility(group.getVisibility())
                .requiresJoinApproval(group.getRequiresJoinApproval())
                .build();
    }

    private boolean calculateCanAccess(Group group, User user, boolean isMember, boolean isOwner,
                                       GroupJoinRequest.RequestStatus requestStatus) {
        // Владелец и участники всегда имеют доступ
        if (isOwner || isMember) {
            return true;
        }

        // Проверяем доступ на основе типа видимости группы
        switch (group.getVisibility()) {
            case PUBLIC:
                return true;
            case RESTRICTED:
                // Для ограниченной группы нужна одобренная заявка
                return requestStatus == GroupJoinRequest.RequestStatus.APPROVED;
            case PRIVATE:
                // Для закрытой группы нужно быть участником
                return false;
            default:
                return false;
        }
    }

    @Data
    @Builder
    public static class GroupAccessInfo {
        private boolean canAccess;
        private boolean isMember;
        private boolean isOwner;
        private GroupJoinRequest.RequestStatus joinRequestStatus;
        private Group.VisibilityType visibility;
        private boolean requiresJoinApproval;
    }
}