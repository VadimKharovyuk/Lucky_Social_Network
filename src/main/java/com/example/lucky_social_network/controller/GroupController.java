package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.exception.ResourceNotFoundException;
import com.example.lucky_social_network.model.Group;
import com.example.lucky_social_network.model.GroupPost;
import com.example.lucky_social_network.model.Post;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.GroupContentRepository;
import com.example.lucky_social_network.service.GroupContentService;
import com.example.lucky_social_network.service.GroupService;
import com.example.lucky_social_network.service.PostService;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;
    private final GroupContentRepository groupContentRepository;
    public final GroupContentService groupContentService;
    private final PostService postService;


    @PostMapping("/{groupId}/repost/{postId}")
    public String repostFromGroup(@PathVariable Long groupId, @PathVariable Long postId,
                                  Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username);

        GroupPost originalPost = groupContentService.findById(postId);

        if (originalPost != null && currentUser != null) {
            if (groupService.isMember(currentUser.getId(), groupId)) {
                Post repost = new Post();
                repost.setContent(originalPost.getContent());
                repost.setAuthor(currentUser);
                repost.setTimestamp(LocalDateTime.now());
                repost.setImageUrl(originalPost.getImgurImageUrl());
                repost.setRepost(true);
                repost.setOriginalGroupPost(originalPost);

                postService.save(repost);

                originalPost.incrementRepostsCount();
                groupContentService.save(originalPost);
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }

        return "redirect:/groups/" + groupId;
    }

    @PostMapping("/{groupId}/delete-post/{postId}")
    public String deletePostByIdInGroup(@PathVariable Long groupId, @PathVariable Long postId, RedirectAttributes redirectAttributes) {
        try {
            groupContentService.deletePostById(postId);
            redirectAttributes.addFlashAttribute("message", "Пост успешно удален");
        } catch (Exception e) {
            log.error("Error deleting post", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении поста");
        }
        return "redirect:/groups/" + groupId;
    }
    @GetMapping("/{groupId}")
    public String showGroup(@PathVariable Long groupId, Model model) {
        Group group = groupService.getGroupById(groupId);
        User currentUser = userService.getCurrentUser();

        boolean isMember = groupService.isUserMemberOfGroup(currentUser.getId(), groupId);
        boolean canPost = groupService.canUserPostInGroup(currentUser, group);
        boolean isOwner = groupService.isOwner(currentUser.getId(), groupId);

        List<GroupPost> posts = groupContentRepository.findByGroupIdOrderByTimestampDesc(groupId);

        model.addAttribute("group", group);
        model.addAttribute("posts", posts);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isMember", isMember);
        model.addAttribute("canPost", canPost);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("groupId", group);

        Map<Long, Boolean> canRepostMap = new HashMap<>();
        for (GroupPost post : posts) {
            // Разрешаем репост, если пользователь является членом группы и не автором поста
            boolean canRepost = isMember && !post.getAuthor().getId().equals(currentUser.getId());
            canRepostMap.put(post.getId(), canRepost);
        }
        model.addAttribute("canRepostMap", canRepostMap);

        return "groups/view";
    }

    // Метод для отображения фото группы
    @GetMapping("/{groupId}/photo")
    @ResponseBody
    public ResponseEntity<byte[]> getGroupPhoto(@PathVariable Long groupId) {
        try {
            byte[] photoData = groupService.getGroupPhoto(groupId);
            if (photoData != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(photoData);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error retrieving group photo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/update/{groupId}")
    public String showUpdateForm(@PathVariable Long groupId, Model model) {
        try {
            Group group = groupService.getGroupById(groupId);
            User currentUser = userService.getCurrentUser();
            boolean isOwner = groupService.isOwner(currentUser.getId(), groupId);

            if (!isOwner) {
                return "redirect:/groups/" + groupId + "?error=Not+authorized";
            }

            model.addAttribute("group", group);
            return "groups/updateGroup";
        } catch (ResourceNotFoundException e) {
            return "redirect:/groups?error=Group+not+found";
        }
    }


    @PostMapping("/update/{groupId}")
    public String updateGroup(
            @PathVariable Long groupId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile photo,
            RedirectAttributes redirectAttributes) {
        try {
            byte[] photoData = photo != null && !photo.isEmpty() ? photo.getBytes() : null;
            Group updatedGroup = groupService.updateGroup(groupId, name, description, photoData);
            redirectAttributes.addFlashAttribute("message", "Group updated successfully");
            return "redirect:/groups/" + updatedGroup.getId();
        } catch (IOException e) {
            log.error("Error processing photo upload", e);
            redirectAttributes.addFlashAttribute("error", "Error uploading photo");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Group not found");
        } catch (RuntimeException e) {
            log.error("Error updating group", e);
            redirectAttributes.addFlashAttribute("error", "Error updating group");
        }
        return "redirect:/groups/update/" + groupId;
    }


    @PostMapping("/{groupId}/join")
    public String joinGroup(@PathVariable Long groupId) {
        User currentUser = userService.getCurrentUser();
        Group group = groupService.getGroupById(groupId);

        if (group.getType() == Group.GroupType.INTERACTIVE) {
            groupService.addMember(group, currentUser);

        } else {
            groupService.subscribeToGroup(group, currentUser);
        }
        return "redirect:/groups/" + groupId;
    }

    @GetMapping("/my")
    public String getCurrentUserGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String type,
            Model model) {

        Page<Group> groupPage = groupService.getCurrentUserGroups(page, size, sortBy, sortDirection, type);
        List<Group> allGroups = groupService.getAllGroups();
        User currentUser = userService.getCurrentUser();

        model.addAttribute("groups", groupPage.getContent());
        model.addAttribute("allGroups", allGroups);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", groupPage.getTotalPages());
        model.addAttribute("totalItems", groupPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("currentType", type);
        model.addAttribute("currentUser", currentUser);

        return "groups/my-groups";
    }

    @GetMapping("/all")
    public String listGroups(Model model) {
        User currentUser = userService.getCurrentUser();
        model.addAttribute("groups", groupService.getAllGroups());
        model.addAttribute("currentUser", currentUser);

        return "groups/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Long CurrentUser = userService.getCurrentUser().getId();
        model.addAttribute("group", new Group());
        model.addAttribute("currentUser", CurrentUser);
        return "groups/create";
    }

    @PostMapping("/create")
    public String createGroup(@ModelAttribute Group group) {
        User currentUser;
        currentUser = userService.getCurrentUser();
        groupService.createGroup(group, currentUser);
        return "redirect:/groups/my";
    }




    @PostMapping("/{groupId}/leave")
    public String leaveGroup(@PathVariable Long groupId) {
        Group group = groupService.getGroupById(groupId);
        User currentUser = userService.getCurrentUser();

        if (group.getType() == Group.GroupType.INTERACTIVE) {
            groupService.leaveGroup(group, currentUser);
        } else {
            groupService.unsubscribeFromGroup(group, currentUser);
        }

        return "redirect:/groups/" + groupId;
    }

    @PostMapping("/{groupId}/post")
    public String createPost(@PathVariable Long groupId,
                             @RequestParam("content") String content,
                             @RequestParam(value = "image", required = false) MultipartFile image,
                             RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();

            byte[] imageData = null;
            if (image != null && !image.isEmpty()) {
                imageData = image.getBytes();
            }

            GroupPost post = groupService.createPost(groupId, currentUser, content, imageData);
            redirectAttributes.addFlashAttribute("message", "Post created successfully");
        } catch (IOException e) {
            log.error("Error processing image upload", e);
            redirectAttributes.addFlashAttribute("error", "Error uploading image");
        } catch (RuntimeException e) {
            log.error("Error creating post", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/groups/" + groupId;
    }

    @PostMapping("/delete/{groupId}")
    public String deleteGroup(@PathVariable Long groupId) {
        Group group = groupService.getGroupById(groupId);
        User currentUser = userService.getCurrentUser();

        if (!group.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only group owner can delete the group");
        }
        groupService.deleteGroupById(groupId);
        return "redirect:/groups/all";
    }


}