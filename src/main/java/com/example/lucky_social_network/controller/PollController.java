package com.example.lucky_social_network.controller;

import com.example.lucky_social_network.dto.PollCreateDTO;
import com.example.lucky_social_network.dto.PollResponseDTO;
import com.example.lucky_social_network.dto.PollUpdateDTO;
import com.example.lucky_social_network.dto.PollVoteDTO;
import com.example.lucky_social_network.exception.PollNotFoundException;
import com.example.lucky_social_network.model.Group;
import com.example.lucky_social_network.model.Poll;
import com.example.lucky_social_network.service.GroupService;
import com.example.lucky_social_network.service.PollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/groups/{groupId}/polls")
@RequiredArgsConstructor
public class PollController {
    private final PollService pollService;
    private final GroupService groupService;

    @GetMapping("/create")
    public String showCreatePollForm(@PathVariable Long groupId, Model model) {
        Group group = groupService.getGroupById(groupId);

        PollCreateDTO pollCreateDTO = new PollCreateDTO();
        pollCreateDTO.setGroupId(groupId);
        pollCreateDTO.setMinimumVotesToShow(0); // Устанавливаем значение по умолчанию
        pollCreateDTO.setMaxChoices(1); // Устанавливаем значение по умолчанию
        pollCreateDTO.setMultipleChoice(false);
        pollCreateDTO.setAnonymous(false);
        pollCreateDTO.setRequiresVerification(false);
        pollCreateDTO.setOneVotePerIp(false);

        model.addAttribute("group", group);
        model.addAttribute("pollCreateDTO", pollCreateDTO);
        model.addAttribute("pollStatuses", Poll.PollStatus.values());
        return "polls/create";
    }

    @PostMapping("/create")
    public String createPoll(@PathVariable Long groupId,
                             @Valid @ModelAttribute PollCreateDTO pollCreateDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "polls/create";
        }

        try {
            pollCreateDTO.setGroupId(groupId);
            PollResponseDTO createdPoll = pollService.createPoll(pollCreateDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Poll created successfully!");
            return "redirect:/groups/" + groupId + "/polls/" + createdPoll.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create poll: " + e.getMessage());
            return "redirect:/groups/" + groupId + "/polls/create";
        }
    }

    @GetMapping
    public String getGroupPolls(@PathVariable Long groupId, Model model) {
        List<PollResponseDTO> polls = pollService.getAllPolls(groupId);
        model.addAttribute("polls", polls);
        model.addAttribute("groupId", groupId);
        return "polls/list";
    }

    @PostMapping("/{pollId}/vote")
    public String vote(@PathVariable Long groupId, // добавляем groupId
                       @PathVariable Long pollId,
                       @ModelAttribute PollVoteDTO voteDTO,
                       RedirectAttributes redirectAttributes) {
        try {
            pollService.votePoll(pollId, voteDTO.getOptionIds());
            redirectAttributes.addFlashAttribute("successMessage", "Vote recorded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to vote: " + e.getMessage());
        }
        return "redirect:/groups/" + groupId + "/polls/" + pollId;
    }

    @GetMapping("/{pollId}")
    public String getPoll(@PathVariable Long groupId,  // добавляем groupId
                          @PathVariable Long pollId,
                          Model model) {
        try {
            PollResponseDTO poll = pollService.getPoll(pollId);
            model.addAttribute("poll", poll);
            model.addAttribute("voteDTO", new PollVoteDTO());
            model.addAttribute("groupId", groupId);
            return "polls/view";
        } catch (PollNotFoundException e) {
            return "errors/not-found";
        }
    }

    @GetMapping("/{pollId}/edit")
    public String showEditForm(@PathVariable Long groupId,  // добавляем groupId
                               @PathVariable Long pollId,
                               Model model) {
        try {
            PollResponseDTO poll = pollService.getPoll(pollId);
            PollUpdateDTO updateDTO = new PollUpdateDTO();
            updateDTO.setQuestion(poll.getQuestion());
            updateDTO.setDescription(poll.getDescription());
            updateDTO.setEndsAt(poll.getEndsAt());
            updateDTO.setStatus(poll.getStatus());

            model.addAttribute("pollId", pollId);
            model.addAttribute("groupId", groupId);
            model.addAttribute("pollUpdateDTO", updateDTO);
            model.addAttribute("pollStatuses", Poll.PollStatus.values());
            return "polls/edit";
        } catch (PollNotFoundException e) {
            return "errors/not-found";
        }
    }

    @PostMapping("/{pollId}/edit")
    public String updatePoll(@PathVariable Long groupId,  // добавляем groupId
                             @PathVariable Long pollId,
                             @Valid @ModelAttribute PollUpdateDTO updateDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "polls/edit";
        }

        try {
            pollService.updatePoll(pollId, updateDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Poll updated successfully!");
            return "redirect:/groups/" + groupId + "/polls/" + pollId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update poll: " + e.getMessage());
            return "redirect:/groups/" + groupId + "/polls/" + pollId + "/edit";
        }
    }

    @PostMapping("/{pollId}/delete")
    public String deletePoll(@PathVariable Long groupId,  // добавляем groupId
                             @PathVariable Long pollId,
                             RedirectAttributes redirectAttributes) {
        try {
            pollService.deletePoll(pollId);
            redirectAttributes.addFlashAttribute("successMessage", "Poll deleted successfully!");
            return "redirect:/groups/" + groupId + "/polls";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete poll: " + e.getMessage());
            return "redirect:/groups/" + groupId + "/polls/" + pollId;
        }
    }
}
