package com.example.lucky_social_network.controller;


import com.example.lucky_social_network.model.Group;
import com.example.lucky_social_network.model.GroupMembership;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.GroupMembershipService;
import com.example.lucky_social_network.service.GroupService;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;

@Controller
@RequestMapping("/groups-admin")
@RequiredArgsConstructor
public class GroupMembershipController {
    private final GroupMembershipService membershipService;
    private final GroupService groupService;
    private final UserService userService;

    @GetMapping("/{groupId}/members")
    public String showMembers(@PathVariable Long groupId, Model model) {
        User currentUser = userService.getCurrentUser();
        Group group = groupService.getGroupById(groupId);

        model.addAttribute("group", group);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("roles", GroupMembership.GroupRole.values());

        return "groups-admin/members";
    }

    @PostMapping("/{groupId}/members/{userId}/role")
    public String assignRole(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestParam GroupMembership.GroupRole role,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = userService.getCurrentUser();
            membershipService.assignRole(groupId, userId, role, currentUser);
            redirectAttributes.addFlashAttribute("success",
                    "Роль успешно обновлена");
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error",
                    "У вас нет прав для назначения этой роли");
        }

        return "redirect:/groups-admin/" + groupId + "/members";
    }

//    @PostMapping("/{groupId}/members/{userId}/remove")
//    public String removeMember(
//            @PathVariable Long groupId,
//            @PathVariable Long userId,
//            RedirectAttributes redirectAttributes) {
//
//        try {
//            User currentUser = userService.getCurrentUser();
//            membershipService.removeMember(groupId, userId, currentUser);
//            redirectAttributes.addFlashAttribute("success",
//                    "Участник успешно удален из группы");
//        } catch (AccessDeniedException e) {
//            redirectAttributes.addFlashAttribute("error",
//                    "У вас нет прав для удаления участника");
//        }
//
//        return "redirect:/groups-admin/" + groupId + "/members";
//    }
}