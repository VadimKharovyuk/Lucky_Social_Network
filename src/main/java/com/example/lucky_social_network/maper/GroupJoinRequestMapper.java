package com.example.lucky_social_network.maper;

import com.example.lucky_social_network.dto.GroupJoinRequestDto;
import com.example.lucky_social_network.model.GroupJoinRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupJoinRequestMapper {

    public GroupJoinRequestDto toDto(GroupJoinRequest request) {
        if (request == null) {
            return null;
        }

        return GroupJoinRequestDto.builder()
                .id(request.getId())
                .groupId(request.getGroup().getId())
                .groupName(request.getGroup().getName())
                .userId(request.getUser().getId())
                .username(request.getUser().getUsername())
                .createdAt(request.getCreatedAt())
                .status(request.getStatus())
                .message(request.getMessage())
                .processedAt(request.getProcessedAt())
                .build();
    }

    public List<GroupJoinRequestDto> toDtoList(List<GroupJoinRequest> requests) {
        if (requests == null) {
            return null;
        }

        return requests.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Опциональный метод для создания сущности из DTO (если потребуется)
    public GroupJoinRequest toEntity(GroupJoinRequestDto dto) {
        if (dto == null) {
            return null;
        }

        GroupJoinRequest request = new GroupJoinRequest();
        request.setId(dto.getId());
        request.setCreatedAt(dto.getCreatedAt());
        request.setStatus(dto.getStatus());
        request.setMessage(dto.getMessage());
        request.setProcessedAt(dto.getProcessedAt());

        // Примечание: group и user нужно будет установить отдельно,
        // так как они требуют загрузки из базы данных

        return request;
    }
}