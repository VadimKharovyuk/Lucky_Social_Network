package com.example.lucky_social_network.service;

import com.example.lucky_social_network.dto.PollCreateDTO;
import com.example.lucky_social_network.dto.PollResponseDTO;
import com.example.lucky_social_network.dto.PollUpdateDTO;
import com.example.lucky_social_network.exception.GroupNotFoundException;
import com.example.lucky_social_network.exception.PollNotActiveException;
import com.example.lucky_social_network.exception.PollNotFoundException;
import com.example.lucky_social_network.exception.PostNotFoundException;
import com.example.lucky_social_network.maper.PollMapper;
import com.example.lucky_social_network.model.*;
import com.example.lucky_social_network.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PollServiceImpl implements PollService {
    private final PollRepository pollRepository;
    private final PollMapper pollMapper;
    private final UserService userService;
    private final PollVoteRepository pollVoteRepository;
    private final PollOptionRepository pollOptionRepository;
    private final GroupRepository groupRepository;
    private final GroupContentRepository groupPostRepository;

    @Override
    @Transactional
    public PollResponseDTO createPoll(PollCreateDTO createDTO) {
        User currentUser = userService.getCurrentUser();
        Group group = groupRepository.findById(createDTO.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException(createDTO.getGroupId()));

        Poll poll = pollMapper.toPoll(createDTO, group, currentUser);

        // Обработка связи с постом
        handlePostConnection(poll, createDTO, group, currentUser);

        poll = pollRepository.save(poll);
        return pollMapper.toPollResponseDTO(poll, currentUser, false, true);
    }

    @Override
    @Transactional(readOnly = true)
    public PollResponseDTO getPoll(Long id) {
        User currentUser = userService.getCurrentUser();
        Poll poll = pollRepository.findById(id)
                .orElseThrow(() -> new PollNotFoundException(id));

        boolean hasVoted = hasUserVoted(poll, currentUser);
        boolean canVote = canUserVote(poll, currentUser);

        return pollMapper.toPollResponseDTO(poll, currentUser, hasVoted, canVote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PollResponseDTO> getAllPolls(Long groupId) {
        User currentUser = userService.getCurrentUser();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        List<Poll> polls = pollRepository.findAllByGroupOrderByCreatedAtDesc(group);

        return polls.stream()
                .map(poll -> {
                    boolean hasVoted = hasUserVoted(poll, currentUser);
                    boolean canVote = canUserVote(poll, currentUser);
                    return pollMapper.toPollResponseDTO(poll, currentUser, hasVoted, canVote);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePoll(Long id) throws AccessDeniedException {
        Poll poll = pollRepository.findById(id)
                .orElseThrow(() -> new PollNotFoundException(id));

        User currentUser = userService.getCurrentUser();
        if (!poll.getCreatedBy().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to delete this poll");
        }

        // Сначала удаляем все голоса для этого опроса
        pollVoteRepository.deleteByPoll(poll);

        // Удаляем все варианты ответов
        pollOptionRepository.deleteByPoll(poll);

        // Теперь можно удалить сам опрос
        pollRepository.delete(poll);
    }

    @Override
    @Transactional
    public void votePoll(Long pollId, List<Long> optionIds) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new PollNotFoundException(pollId));
        User currentUser = userService.getCurrentUser();

        validateVoting(poll, optionIds, currentUser);

        List<PollOption> selectedOptions = pollOptionRepository.findAllById(optionIds);
        validateOptions(poll, selectedOptions, optionIds);

        processVotes(poll, selectedOptions, currentUser);
        updatePollStatistics(poll);
    }

    @Override
    @Transactional
    public void updatePoll(Long id, PollUpdateDTO updateDTO) throws AccessDeniedException {
        Poll poll = pollRepository.findById(id)
                .orElseThrow(() -> new PollNotFoundException(id));
        User currentUser = userService.getCurrentUser();

        validatePollUpdate(poll, currentUser);
        pollMapper.updatePollFromDTO(poll, updateDTO, currentUser);

        pollRepository.save(poll);
    }

    @Override
    @Transactional
    public void deleteAllPollsByGroupId(Long groupId) {
        try {
            log.info("Starting deletion of polls for group: {}", groupId);

            // 1. Сначала находим все опросы группы
            List<Poll> polls = pollRepository.findByGroupId(groupId);

            // 2. Собираем ID всех опций этих опросов
            List<Long> pollIds = polls.stream()
                    .map(Poll::getId)
                    .collect(Collectors.toList());

            // 3. Удаляем голоса
            if (!pollIds.isEmpty()) {
                pollVoteRepository.deleteAllByPollIdIn(pollIds);
                log.info("Deleted votes for polls: {}", pollIds);
            }

            // 4. Удаляем опции
            pollOptionRepository.deleteAllByPollIdIn(pollIds);
            log.info("Deleted options for polls: {}", pollIds);

            // 5. Удаляем сами опросы
            pollRepository.deleteByGroupId(groupId);
            log.info("Deleted polls for group: {}", groupId);

        } catch (Exception e) {
            log.error("Error deleting polls for group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Failed to delete polls for group: " + groupId, e);
        }
    }


    // Вспомогательные методы
    private void handlePostConnection(Poll poll, PollCreateDTO createDTO, Group group, User currentUser) {
        if (createDTO.getPostId() != null) {
            GroupPost existingPost = groupPostRepository.findById(createDTO.getPostId())
                    .orElseThrow(() -> new PostNotFoundException(createDTO.getPostId()));

            if (!existingPost.getGroup().getId().equals(group.getId())) {
                throw new IllegalArgumentException("Post does not belong to the specified group");
            }
            poll.setPost(existingPost);
        } else if (StringUtils.hasText(createDTO.getPostContent())) {
            GroupPost newPost = createNewPost(createDTO.getPostContent(), group, currentUser);
            poll.setPost(groupPostRepository.save(newPost));
        }
    }

    private GroupPost createNewPost(String content, Group group, User author) {
        GroupPost post = new GroupPost();
        post.setContent(content);
        post.setGroup(group);
        post.setAuthor(author);
        post.setPinnedAt(LocalDateTime.now());
        return post;
    }

    private boolean hasUserVoted(Poll poll, User user) {
        return pollVoteRepository.existsByPollAndUser(poll, user);
    }

    private boolean canUserVote(Poll poll, User currentUser) {
        return poll.getStatus() == Poll.PollStatus.ACTIVE &&
                !hasUserVoted(poll, currentUser) &&
                (poll.getEndsAt() == null || poll.getEndsAt().isAfter(LocalDateTime.now()));
    }

    private void validateVoting(Poll poll, List<Long> optionIds, User currentUser) {
        if (poll.getStatus() != Poll.PollStatus.ACTIVE) {
            throw new PollNotActiveException(poll.getId());  // Используем getId() из объекта poll
        }

        if (poll.getEndsAt() != null && poll.getEndsAt().isBefore(LocalDateTime.now())) {
            poll.setStatus(Poll.PollStatus.ENDED);
            pollRepository.save(poll);
            throw new PollNotActiveException(poll.getId());  // Используем getId() из объекта poll
        }

        if (!poll.isMultipleChoice() && hasUserVoted(poll, currentUser)) {
            throw new RuntimeException("User has already voted in this poll");
        }

        if (poll.getMaxChoices() != null && optionIds.size() > poll.getMaxChoices()) {
            throw new RuntimeException("Too many options selected. Maximum allowed: " + poll.getMaxChoices());
        }
    }

    private void validateOptions(Poll poll, List<PollOption> selectedOptions, List<Long> requestedOptionIds) {
        if (selectedOptions.size() != requestedOptionIds.size()) {
            throw new RuntimeException("Some options were not found");
        }

        for (PollOption option : selectedOptions) {
            if (!option.getPoll().getId().equals(poll.getId())) {
                throw new RuntimeException("Option " + option.getId() + " does not belong to poll " + poll.getId());
            }
        }
    }

    private void processVotes(Poll poll, List<PollOption> selectedOptions, User currentUser) {
        String ipAddress = "127.0.0.1"; // В реальном приложении получаем IP из запроса

        for (PollOption option : selectedOptions) {
            PollVote vote = new PollVote();
            vote.setPoll(poll);
            vote.setOption(option);
            vote.setUser(currentUser);
            vote.setIpAddress(ipAddress);
            vote.setVotedAt(LocalDateTime.now());
            vote.setVerified(!poll.isRequiresVerification());

            pollVoteRepository.save(vote);

            option.setVotesCount(option.getVotesCount() + 1);
            option.getVoters().add(currentUser);
            pollOptionRepository.save(option);
        }
    }

    private void updatePollStatistics(Poll poll) {
        poll.setTotalVotes(pollVoteRepository.countByPoll(poll));
        poll.setUniqueVoters(pollVoteRepository.countDistinctUsersByPoll(poll));
        pollRepository.save(poll);
    }

    private void validatePollUpdate(Poll poll, User currentUser) throws AccessDeniedException {
        if (poll.getStatus() == Poll.PollStatus.ENDED || poll.getStatus() == Poll.PollStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update poll that has ended or been cancelled");
        }

        if (!poll.getCreatedBy().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to update this poll");
        }
    }
}
