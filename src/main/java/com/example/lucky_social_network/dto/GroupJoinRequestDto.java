package com.example.lucky_social_network.dto;

import com.example.lucky_social_network.model.GroupJoinRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupJoinRequestDto {
    private Long id;
    private Long groupId;
    private String groupName;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;
    private GroupJoinRequest.RequestStatus status;
    private String message;
    private LocalDateTime processedAt;
}
