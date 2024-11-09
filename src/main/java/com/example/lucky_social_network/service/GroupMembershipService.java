package com.example.lucky_social_network.service;

import com.example.lucky_social_network.exception.GroupNotFoundException;
import com.example.lucky_social_network.exception.UserNotFoundException;
import com.example.lucky_social_network.model.Group;
import com.example.lucky_social_network.model.GroupMembership;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.GroupMembershipRepository;
import com.example.lucky_social_network.repository.GroupRepository;
import com.example.lucky_social_network.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupMembershipService {
    private final GroupMembershipRepository membershipRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public void assignRole(Long groupId, Long userId, GroupMembership.GroupRole newRole, User actionBy) throws AccessDeniedException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Проверяем права на назначение роли
        if (!canAssignRole(actionBy, group, newRole)) {
            throw new AccessDeniedException("You don't have permission to assign this role");
        }

        GroupMembership membership = membershipRepository
                .findByGroupAndUser(group, user)
                .orElseGet(() -> {
                    GroupMembership newMembership = new GroupMembership();
                    newMembership.setGroup(group);
                    newMembership.setUser(user);
                    return newMembership;
                });

        membership.setRole(newRole);
        membership.setRoleAssignedAt(LocalDateTime.now());

        membershipRepository.save(membership);
    }

    private boolean canAssignRole(User actionBy, Group group, GroupMembership.GroupRole roleToAssign) {


        // Владелец группы может назначать админов и модераторов
        if (group.isOwner(actionBy)) {
            return roleToAssign != GroupMembership.GroupRole.OWNER;
        }

        // Админ группы может назначать только модераторов
        if (group.isAdmin(actionBy)) {
            return roleToAssign == GroupMembership.GroupRole.MODERATOR;
        }

        return false;
    }
}
