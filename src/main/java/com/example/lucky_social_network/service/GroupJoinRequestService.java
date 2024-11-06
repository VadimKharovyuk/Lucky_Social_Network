package com.example.lucky_social_network.service;

import com.example.lucky_social_network.dto.GroupJoinRequestDto;
import com.example.lucky_social_network.model.GroupJoinRequest;
import com.example.lucky_social_network.model.User;

import java.util.List;

public interface GroupJoinRequestService {
    // Создание запроса на вступление в группу
    GroupJoinRequestDto createJoinRequest(Long groupId, User requester, String message);

    // Получение всех запросов для группы по статусу
    List<GroupJoinRequestDto> getGroupRequests(Long groupId, String status);

    // Получение всех запросов пользователя
    List<GroupJoinRequestDto> getUserRequests(Long userId, String status);

    // Одобрение запроса
    GroupJoinRequestDto approveRequest(Long requestId, User approver);

    // Отклонение запроса
    GroupJoinRequestDto rejectRequest(Long requestId, User approver);

    // Отмена запроса пользователем
    void cancelRequest(Long requestId, User requester);

    // Проверка существования активного запроса
    boolean hasActiveRequest(Long groupId, Long userId);

    // Добавляем новый метод для подсчета ожидающих заявок
    long countPendingRequests(Long groupId);


    void debugGroupRequests(Long groupId);

    GroupJoinRequest.RequestStatus getRequestStatus(Long groupId, Long id);
}
