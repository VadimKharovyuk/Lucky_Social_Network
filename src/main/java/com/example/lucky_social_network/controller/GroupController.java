package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.AdDTO;
import com.example.lucky_social_network.dto.PollResponseDTO;
import com.example.lucky_social_network.exception.ResourceNotFoundException;
import com.example.lucky_social_network.model.*;
import com.example.lucky_social_network.repository.GroupContentRepository;
import com.example.lucky_social_network.service.*;
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
    private final PollServiceImpl pollService;
    private final GroupJoinRequestService groupJoinRequestService;
    private final AdService adService;

    @GetMapping("/{groupId}")
    public String showGroup(@PathVariable Long groupId, Model model) {
        Group group = groupService.getGroupById(groupId);
        User currentUser = userService.getCurrentUser();

        // Получаем всю информацию о доступе через GroupAccessInfo
        GroupService.GroupAccessInfo accessInfo = groupService.getGroupAccessInfo(groupId, currentUser);

        // Проверяем базовый доступ к группе
        if (!accessInfo.isCanAccess()) {
            if (group.getVisibility() == Group.VisibilityType.PRIVATE) {
                // Для закрытой группы
                model.addAttribute("group", group);
                model.addAttribute("accessInfo", accessInfo);
                return "groups/access-restricted";
            } else if (group.getVisibility() == Group.VisibilityType.RESTRICTED) {
                // Проверяем статус заявки для ограниченной группы
                GroupJoinRequest.RequestStatus requestStatus =
                        groupJoinRequestService.getRequestStatus(groupId, currentUser.getId());

                if (requestStatus == null) {
                    // Нет заявки - показываем форму подачи
                    model.addAttribute("group", group);
                    model.addAttribute("accessInfo", accessInfo);
                    return "groups/access-restricted";
                } else if (requestStatus != GroupJoinRequest.RequestStatus.APPROVED) {
                    // Есть заявка, но не одобрена
                    model.addAttribute("group", group);
                    model.addAttribute("requestStatus", requestStatus);
                    model.addAttribute("accessInfo", accessInfo);
                    return "groups/access-restricted";
                }
            }
        }

        // Если есть доступ, получаем контент
        boolean canPost = groupService.canUserPostInGroup(currentUser, group);
        List<GroupPost> posts = groupContentRepository.findByGroupIdOrderByTimestampDesc(groupId);
        List<PollResponseDTO> polls = pollService.getAllPolls(groupId);

        log.info("Showing group {}, currentUser: {}, isOwner: {}, visibility: {}",
                groupId, currentUser.getUsername(), accessInfo.isOwner(), group.getVisibility());

        // Для владельца добавляем информацию о заявках
        if (accessInfo.isOwner()) {
            try {
                long pendingRequestsCount = groupJoinRequestService.countPendingRequests(groupId);
                log.info("Found {} pending requests for group {}", pendingRequestsCount, groupId);
                model.addAttribute("pendingRequestsCount", pendingRequestsCount);
            } catch (Exception e) {
                log.error("Error processing pending requests: ", e);
                model.addAttribute("pendingRequestsCount", 0L);
            }
        }

        // Карта для проверки возможности репоста
        Map<Long, Boolean> canRepostMap = new HashMap<>();
        for (GroupPost post : posts) {
            boolean canRepost = accessInfo.isMember() &&
                    !post.getAuthor().getId().equals(currentUser.getId());
            canRepostMap.put(post.getId(), canRepost);
        }
        List<AdDTO> groupAds = adService.getActiveAdsByGroup(groupId);
        model.addAttribute("advertisements", groupAds);

        // Добавляем все в модель
        model.addAttribute("group", group);
        model.addAttribute("posts", posts);
        model.addAttribute("polls", polls);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("accessInfo", accessInfo);
        model.addAttribute("canPost", canPost);
        model.addAttribute("canRepostMap", canRepostMap);
        model.addAttribute("isOwner", accessInfo.isOwner());

        return "groups/view";
    }


    @PostMapping("/{groupId}/join")
    public String joinGroup(@PathVariable Long groupId,
                            RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        Group group = groupService.getGroupById(groupId);

        try {
            if (group.getType() == Group.GroupType.INTERACTIVE) {
                // Если группа требует одобрения и это интерактивная группа
                if (group.getRequiresJoinApproval()) {
                    // Создаем запрос на вступление
                    groupJoinRequestService.createJoinRequest(groupId, currentUser, null);
                    redirectAttributes.addFlashAttribute("message",
                            "Запрос на вступление отправлен. Ожидайте одобрения администратора.");
                } else {
                    // Если одобрение не требуется, используем существующую логику
                    groupService.addMember(group, currentUser);
                    redirectAttributes.addFlashAttribute("message",
                            "Вы успешно присоединились к группе.");
                }
            } else {
                // Для неинтерактивных групп используем существующую логику подписки
                groupService.subscribeToGroup(group, currentUser);
                redirectAttributes.addFlashAttribute("message",
                        "Вы успешно подписались на группу.");
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/groups/" + groupId;
    }




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
        Long currentUser = userService.getCurrentUser().getId();
        Group group = new Group();

        // Добавляем все возможные значения enum для выбора в форме
        model.addAttribute("visibilityTypes", Group.VisibilityType.values());
        model.addAttribute("groupTypes", Group.GroupType.values());
        model.addAttribute("group", group);
        model.addAttribute("currentUser", currentUser);

        return "groups/create";
    }

    @PostMapping("/create")
    public String createGroup(@ModelAttribute Group group) {
        User currentUser = userService.getCurrentUser();

        // Устанавливаем параметры приватности в зависимости от типа группы
        if (group.getVisibility() == Group.VisibilityType.PUBLIC) {
            group.setRequiresJoinApproval(false);
        } else {
            group.setRequiresJoinApproval(true);
        }

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