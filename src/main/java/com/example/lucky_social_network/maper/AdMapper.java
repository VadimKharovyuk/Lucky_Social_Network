package com.example.lucky_social_network.maper;

import com.example.lucky_social_network.dto.AdCreateRequest;
import com.example.lucky_social_network.dto.AdDTO;
import com.example.lucky_social_network.dto.AdScheduleDTO;
import com.example.lucky_social_network.exception.GroupNotFoundException;
import com.example.lucky_social_network.model.Ad;
import com.example.lucky_social_network.model.Group;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class AdMapper {
    private final GroupRepository groupRepository;


    public Ad toEntity(AdCreateRequest request, User currentUser) {
        Ad ad = new Ad();

        // Основные поля
        ad.setTitle(request.title());
        ad.setContent(request.content());
        ad.setImageUrl(request.imageUrl());
        ad.setExternalUrl(request.externalUrl());
        ad.setOwner(currentUser);

        if (request.owningGroupId() != null) {
            Group group = groupRepository.findById(request.owningGroupId())
                    .orElseThrow(() -> new GroupNotFoundException(request.owningGroupId()));
            ad.setOwningGroup(group);
        }

        ad.setStartTime(request.startTime());
        ad.setEndTime(request.endTime());

        // Добавляем расписания через метод addSchedule из Ad
        if (request.schedules() != null && !request.schedules().isEmpty()) {
            for (AdScheduleDTO scheduleDTO : request.schedules()) {
                ad.addSchedule(
                        scheduleDTO.getDayOfWeek(),
                        scheduleDTO.getStartTime(),
                        scheduleDTO.getEndTime()
                );
            }
        } else {
            // Добавляем расписания по умолчанию
            ad.addSchedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0));
            ad.addSchedule(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(18, 0));
        }

        return ad;
    }
    
    public AdDTO toDTO(Ad ad) {
        List<AdScheduleDTO> scheduleDTOs = ad.getSchedules().stream()
                .map(schedule -> new AdScheduleDTO(
                        schedule.getDayOfWeek(),
                        schedule.getStartTime(),
                        schedule.getEndTime()
                ))
                .collect(Collectors.toList());

        return new AdDTO(
                ad.getId(),
                ad.getTitle(),
                ad.getContent(),
                ad.getImageUrl(),
                ad.getExternalUrl(),
                ad.getOwner().getId(),
                ad.getOwningGroup() != null ? ad.getOwningGroup().getId() : null,
                ad.getTargetGroups().stream()
                        .map(Group::getId)
                        .collect(Collectors.toSet()),
                ad.getStartTime(),
                ad.getEndTime(),
                scheduleDTOs,
                ad.getStatus(),
                ad.isActive(),
                ad.getCreatedAt(),
                ad.getUpdatedAt()
        );
    }
}