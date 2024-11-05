package com.example.lucky_social_network.service;

import com.example.lucky_social_network.dto.PollCreateDTO;
import com.example.lucky_social_network.dto.PollResponseDTO;
import com.example.lucky_social_network.dto.PollUpdateDTO;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface PollService {
    PollResponseDTO createPoll(PollCreateDTO createDTO);

    PollResponseDTO getPoll(Long id);

    List<PollResponseDTO> getAllPolls(Long groupId);

    void deletePoll(Long id) throws AccessDeniedException;

    void votePoll(Long pollId, List<Long> optionIds);

    void updatePoll(Long id, PollUpdateDTO updateDTO) throws AccessDeniedException;
}
