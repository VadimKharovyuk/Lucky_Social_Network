package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.GroupJoinRequestDto;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.GroupJoinRequestService;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/group-requests")
@RequiredArgsConstructor
public class GroupJoinRequestController {

    private final GroupJoinRequestService joinRequestService;
    private final UserService userService;

    @GetMapping("/view/{groupId}")
    public String viewJoinRequests(@PathVariable Long groupId,
                                   @RequestParam(defaultValue = "PENDING") String status,
                                   Model model) {
        User currentUser = userService.getCurrentUser();
        log.info("Viewing join requests for group {} with status {}", groupId, status);

        List<GroupJoinRequestDto> requests = joinRequestService.getGroupRequests(groupId, status);
        model.addAttribute("requests", requests);
        model.addAttribute("groupId", groupId);
        model.addAttribute("currentUser", currentUser);

        return "groups/join-requests";
    }

    @PostMapping("/approve/{requestId}")
    public String approveRequest(@PathVariable Long requestId) {
        User currentUser = userService.getCurrentUser();
        log.info("Approving request {} by user {}", requestId, currentUser.getUsername());

        GroupJoinRequestDto request = joinRequestService.approveRequest(requestId, currentUser);
        return "redirect:/group-requests/view/" + request.getGroupId();
    }

    @PostMapping("/reject/{requestId}")
    public String rejectRequest(@PathVariable Long requestId) {
        User currentUser = userService.getCurrentUser();
        log.info("Rejecting request {} by user {}", requestId, currentUser.getUsername());

        GroupJoinRequestDto request = joinRequestService.rejectRequest(requestId, currentUser);
        return "redirect:/group-requests/view/" + request.getGroupId();
    }

    @PostMapping("/join/{groupId}")
    public String createJoinRequest(@PathVariable Long groupId,
                                    @RequestParam(required = false) String message) {
        User currentUser = userService.getCurrentUser();
        log.info("Creating join request for group {} by user {}", groupId, currentUser.getUsername());

        joinRequestService.createJoinRequest(groupId, currentUser, message);
        return "redirect:/groups/" + groupId;
    }
}