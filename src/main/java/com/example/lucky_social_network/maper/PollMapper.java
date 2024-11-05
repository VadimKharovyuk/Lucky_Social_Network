package com.example.lucky_social_network.maper;

import com.example.lucky_social_network.dto.*;
import com.example.lucky_social_network.model.Group;
import com.example.lucky_social_network.model.Poll;
import com.example.lucky_social_network.model.PollOption;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PollMapper {
    private final UserService userService;

    public Poll toPoll(PollCreateDTO dto, Group group, User creator) {
        Poll poll = new Poll();
        poll.setGroup(group);
        poll.setQuestion(dto.getQuestion());
        poll.setDescription(dto.getDescription());
        poll.setEndsAt(dto.getEndsAt());
        poll.setMultipleChoice(dto.isMultipleChoice());
        poll.setAnonymous(dto.isAnonymous());
        poll.setMaxChoices(dto.getMaxChoices());
        poll.setMinimumVotesToShow(dto.getMinimumVotesToShow());
        poll.setRequiresVerification(dto.isRequiresVerification());
        poll.setOneVotePerIp(dto.isOneVotePerIp());
        poll.setCreatedAt(LocalDateTime.now());
        poll.setCreatedBy(creator.getId());
        poll.setStatus(Poll.PollStatus.ACTIVE);

        if (dto.getOptions() != null) {
            List<PollOption> options = new ArrayList<>();
            for (int i = 0; i < dto.getOptions().size(); i++) {
                PollOption option = new PollOption();
                option.setText(dto.getOptions().get(i));
                option.setPoll(poll);
                option.setPosition(i);
                option.setVotesCount(0L);
                options.add(option);
            }
            poll.setOptions(options);
        }

        return poll;
    }

    public PollResponseDTO toPollResponseDTO(Poll poll, User currentUser, boolean hasVoted, boolean canVote) {
        PollResponseDTO dto = new PollResponseDTO();
        dto.setId(poll.getId());
        dto.setQuestion(poll.getQuestion());
        dto.setDescription(poll.getDescription());
        dto.setPostId(poll.getPost() != null ? poll.getPost().getId() : null);
        dto.setEndsAt(poll.getEndsAt());
        dto.setMultipleChoice(poll.isMultipleChoice());
        dto.setAnonymous(poll.isAnonymous());
        dto.setMaxChoices(poll.getMaxChoices());
        dto.setTotalVotes(poll.getTotalVotes());
        dto.setUniqueVoters(poll.getUniqueVoters());
        dto.setStatus(poll.getStatus());
        dto.setCreatedAt(poll.getCreatedAt());
        dto.setHasVoted(hasVoted);
        dto.setCanVote(canVote);

        if (poll.getCreatedBy() != null) {
            User creator = userService.getUserById(poll.getCreatedBy());
            dto.setCreatedBy(toUserShortDTO(creator));
        }

        if (poll.getOptions() != null) {
            List<PollOptionResponseDTO> options = poll.getOptions().stream()
                    .map(option -> toPollOptionResponseDTO(option, poll, currentUser))
                    .collect(Collectors.toList());
            dto.setOptions(options);
        }

        return dto;
    }

    private PollOptionResponseDTO toPollOptionResponseDTO(PollOption option, Poll poll, User currentUser) {
        PollOptionResponseDTO dto = new PollOptionResponseDTO();
        dto.setId(option.getId());
        dto.setText(option.getText());
        dto.setVotesCount(option.getVotesCount());

        // Вычисляем процент голосов
        dto.setPercentage(calculatePercentage(option, poll));

        // Проверяем выбор текущего пользователя
        dto.setSelectedByCurrentUser(option.getVoters().contains(currentUser));

        // Добавляем список проголосовавших, если опрос не анонимный и достаточно голосов
        if (shouldShowVoters(option, poll)) {
            dto.setVoters(option.getVoters().stream()
                    .map(this::toUserShortDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private double calculatePercentage(PollOption option, Poll poll) {
        if (poll.getTotalVotes() > 0) {
            double percentage = (option.getVotesCount() * 100.0) / poll.getTotalVotes();
            return Math.round(percentage * 10.0) / 10.0; // Округляем до 1 знака
        }
        return 0.0;
    }

    private boolean shouldShowVoters(PollOption option, Poll poll) {
        return !poll.isAnonymous() &&
                poll.getTotalVotes() >= poll.getMinimumVotesToShow();
    }

    private UserShortDTO toUserShortDTO(User user) {
        if (user == null) {
            return null;
        }
        UserShortDTO dto = new UserShortDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }

    public void updatePollFromDTO(Poll poll, PollUpdateDTO dto, User modifier) {
        if (dto.getQuestion() != null) {
            poll.setQuestion(dto.getQuestion());
        }
        if (dto.getDescription() != null) {
            poll.setDescription(dto.getDescription());
        }
        if (dto.getEndsAt() != null) {
            poll.setEndsAt(dto.getEndsAt());
        }
        if (dto.getStatus() != null) {
            poll.setStatus(dto.getStatus());
        }
        if (dto.getMinimumVotesToShow() != null) {
            poll.setMinimumVotesToShow(dto.getMinimumVotesToShow());
        }

        poll.setLastModifiedAt(LocalDateTime.now());
        poll.setLastModifiedBy(modifier.getId());
    }

    public PollVoteDTO toPollVoteDTO(Poll poll, List<Long> optionIds, String ipAddress) {
        PollVoteDTO dto = new PollVoteDTO();
        dto.setPollId(poll.getId());
        dto.setOptionIds(optionIds);
        dto.setIpAddress(ipAddress);
        return dto;
    }
}