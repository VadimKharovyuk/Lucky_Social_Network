package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.model.Group;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.GroupService;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;

    @GetMapping
    public String listGroups(Model model) {
        model.addAttribute("groups", groupService.getAllGroups());
        return "groups/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("group", new Group());
        return "groups/create";
    }

    @PostMapping("/create")
    public String createGroup(@ModelAttribute Group group) {
        log.info("Attempting to create group: {}", group.getName());

        User currentUser;
        try {
            currentUser = userService.getCurrentUser();
            log.info("Current user: {}", currentUser.getUsername());
        } catch (IllegalStateException e) {
            log.error("Failed to get current user: {}", e.getMessage());
            return "redirect:/login";
        }

        groupService.createGroup(group, currentUser);
        return "redirect:/groups";
    }

    @GetMapping("/{groupId}")
    public String showGroup(@PathVariable Long groupId, Model model) {
        model.addAttribute("group", groupService.getGroupById(groupId));
        return "groups/view";
    }

    @PostMapping("/{groupId}/join")
    public String joinGroup(@PathVariable Long groupId, @AuthenticationPrincipal User currentUser) {
        Group group = groupService.getGroupById(groupId);
        if (group.getType() == Group.GroupType.INTERACTIVE) {
            groupService.addMember(group, currentUser);
        } else {
            groupService.subscribeToGroup(group, currentUser);
        }
        return "redirect:/groups/" + groupId;
    }

    @PostMapping("/{groupId}/leave")
    public String leaveGroup(@PathVariable Long groupId, @AuthenticationPrincipal User currentUser) {
        Group group = groupService.getGroupById(groupId);
        if (group.getType() == Group.GroupType.INTERACTIVE) {
            groupService.leaveGroup(group, currentUser);
        } else {
            groupService.unsubscribeFromGroup(group, currentUser);
        }
        return "redirect:/groups/" + groupId;
    }

    @PostMapping("/{groupId}/post")
    public String createPost(@PathVariable Long groupId, @RequestParam String content, @AuthenticationPrincipal User currentUser) {
        Group group = groupService.getGroupById(groupId);
        groupService.createPost(group, currentUser, content);
        return "redirect:/groups/" + groupId;
    }
}