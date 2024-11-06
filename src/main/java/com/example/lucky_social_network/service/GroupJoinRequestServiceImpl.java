package com.example.lucky_social_network.service;

import com.example.lucky_social_network.dto.GroupJoinRequestDto;
import com.example.lucky_social_network.exception.ResourceNotFoundException;
import com.example.lucky_social_network.exception.UnauthorizedAccessException;
import com.example.lucky_social_network.maper.GroupJoinRequestMapper;
import com.example.lucky_social_network.model.Group;
import com.example.lucky_social_network.model.GroupJoinRequest;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.GroupJoinRequestRepository;
import com.example.lucky_social_network.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupJoinRequestServiceImpl implements GroupJoinRequestService {

    private final GroupJoinRequestRepository requestRepository;
    private final GroupRepository groupRepository;
    private final GroupJoinRequestMapper mapper;


    @Override
    @Transactional
    public GroupJoinRequestDto createJoinRequest(Long groupId, User requester, String message) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        // Проверяем, не является ли пользователь уже членом группы
        if (group.getMembers().contains(requester)) {
            throw new IllegalStateException("User is already a member of this group");
        }

        // Проверяем, нет ли уже активного запроса
        if (hasActiveRequest(groupId, requester.getId())) {
            throw new IllegalStateException("Active join request already exists");
        }

        GroupJoinRequest request = new GroupJoinRequest();
        request.setGroup(group);
        request.setUser(requester);
        request.setMessage(message);
        request.setCreatedAt(LocalDateTime.now());
        request.setStatus(GroupJoinRequest.RequestStatus.PENDING);

        return mapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupJoinRequestDto> getGroupRequests(Long groupId, String status) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        GroupJoinRequest.RequestStatus requestStatus = GroupJoinRequest.RequestStatus.valueOf(status.toUpperCase());
        return mapper.toDtoList(requestRepository.findAllByGroupAndStatus(group, requestStatus));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupJoinRequestDto> getUserRequests(Long userId, String status) {
        GroupJoinRequest.RequestStatus requestStatus = GroupJoinRequest.RequestStatus.valueOf(status.toUpperCase());
        return mapper.toDtoList(requestRepository.findAllByUserAndStatus(
                User.builder().id(userId).build(),
                requestStatus
        ));
    }

    @Override
    @Transactional
    public GroupJoinRequestDto approveRequest(Long requestId, User approver) {
        GroupJoinRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Join request not found"));

        // Проверяем права на одобрение запроса
        if (!request.getGroup().getOwner().getId().equals(approver.getId())) {
            throw new UnauthorizedAccessException("Only group owner can approve requests");
        }

        request.setStatus(GroupJoinRequest.RequestStatus.APPROVED);
        request.setProcessedAt(LocalDateTime.now());

        // Добавляем пользователя в группу
        Group group = request.getGroup();
        group.getMembers().add(request.getUser());
        group.setMembersCount(group.getMembersCount() + 1);
        groupRepository.save(group);

        return mapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public GroupJoinRequestDto rejectRequest(Long requestId, User approver) {
        GroupJoinRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Join request not found"));

        if (!request.getGroup().getOwner().getId().equals(approver.getId())) {
            throw new UnauthorizedAccessException("Only group owner can reject requests");
        }

        request.setStatus(GroupJoinRequest.RequestStatus.REJECTED);
        request.setProcessedAt(LocalDateTime.now());

        return mapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public void cancelRequest(Long requestId, User requester) {
        GroupJoinRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Join request not found"));

        if (!request.getUser().getId().equals(requester.getId())) {
            throw new UnauthorizedAccessException("Only request creator can cancel it");
        }

        requestRepository.delete(request);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveRequest(Long groupId, Long userId) {
        return requestRepository.existsByGroupAndUserAndStatus(
                Group.builder().id(groupId).build(),
                User.builder().id(userId).build(),
                GroupJoinRequest.RequestStatus.PENDING
        );
    }


    @Override
    @Transactional(readOnly = true)
    public long countPendingRequests(Long groupId) {
        try {
            // Добавляем логирование
            log.info("Counting pending requests for group {} with status {}",
                    groupId, GroupJoinRequest.RequestStatus.PENDING.name());

            // Явно преобразуем enum в String
            String status = GroupJoinRequest.RequestStatus.PENDING.name();
            long count = requestRepository.countByGroupIdAndStatusJPQL(groupId, GroupJoinRequest.RequestStatus.valueOf(status));

            log.info("Found {} pending requests for group {}", count, groupId);
            return count;
        } catch (Exception e) {
            log.error("Error counting pending requests for group {}: ", groupId, e);
            e.printStackTrace(); // Добавляем полный стек ошибки
            return 0L;
        }
    }

    // Добавьте метод для отладки
    public void debugGroupRequests(Long groupId) {
        List<GroupJoinRequest> requests = requestRepository.findAllByGroupId(groupId);
        log.info("All requests for group {}: {}", groupId, requests.size());
        requests.forEach(request ->
                log.info("Request: groupId={}, userId={}, status={}",
                        request.getGroup().getId(),
                        request.getUser().getId(),
                        request.getStatus())
        );
    }
}
