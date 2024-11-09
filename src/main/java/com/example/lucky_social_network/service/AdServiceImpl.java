package com.example.lucky_social_network.service;

import com.example.lucky_social_network.dto.AdCreateRequest;
import com.example.lucky_social_network.dto.AdDTO;
import com.example.lucky_social_network.dto.AdUpdateRequest;
import com.example.lucky_social_network.exception.AdNotFoundException;
import com.example.lucky_social_network.exception.GroupNotFoundException;
import com.example.lucky_social_network.maper.AdMapper;
import com.example.lucky_social_network.model.Ad;
import com.example.lucky_social_network.model.Group;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.AdRepository;
import com.example.lucky_social_network.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AdServiceImpl implements AdService {
    private final AdRepository adRepository;
    private final AdMapper adMapper;
    private final GroupRepository groupRepository;


    @Override
    public AdDTO createAd(AdCreateRequest request, User currentUser) throws AccessDeniedException {
        // Проверяем права на создание рекламы
        if (request.owningGroupId() != null) {
            Group group = groupRepository.findById(request.owningGroupId())
                    .orElseThrow(() -> new GroupNotFoundException(request.owningGroupId()));

            if (!canCreateAdForGroup(currentUser, group)) {
                throw new AccessDeniedException(
                        "Only group owner or administrators can create advertisements"
                );
            }
        }

        Ad ad = adMapper.toEntity(request, currentUser);
        log.info("Created ad with {} schedules", ad.getSchedules().size()); // добавьте лог

        ad.setStatus(Ad.AdStatus.DRAFT);
        ad.setActive(false);

        Ad savedAd = adRepository.save(ad);
        log.info("User {} created new advertisement {} with {} schedules",
                currentUser.getId(), savedAd.getId(), savedAd.getSchedules().size());

        return adMapper.toDTO(savedAd);
    }

    private void validateRequest(AdCreateRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.title() == null || request.title().trim().isEmpty()) {
            errors.add("Заголовок обязателен");
        }

        if (request.content() == null || request.content().trim().isEmpty()) {
            errors.add("Содержание обязательно");
        }

        if (request.startTime() == null) {
            errors.add("Время начала обязательно");
        }

        if (request.endTime() == null) {
            errors.add("Время окончания обязательно");
        }

        if (request.startTime() != null && request.endTime() != null &&
                request.endTime().isBefore(request.startTime())) {
            errors.add("Время окончания должно быть позже времени начала");
        }

        if (request.schedules() == null || request.schedules().isEmpty()) {
            errors.add("Должно быть указано хотя бы одно расписание");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Ошибки валидации: " + String.join(", ", errors));
        }
    }

    @Override
    public AdDTO updateAd(Long adId, AdUpdateRequest request) {
        return null;
    }

    @Override
    public AdDTO updateAd(Long adId, AdUpdateRequest request, User currentUser) throws AccessDeniedException {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new AdNotFoundException(adId));

        // Проверяем права на обновление
        if (!canModifyAd(currentUser, ad)) {
            throw new AccessDeniedException(
                    "You don't have permission to update this advertisement"
            );
        }

//        updateAdFromRequest(ad, request);
        Ad updatedAd = adRepository.save(ad);
        return adMapper.toDTO(updatedAd);
    }


    private boolean canCreateAdForGroup(User currentUser, Group group) {
        // Проверяем роли пользователя в группе
        return group.isOwner(currentUser) || // Владелец группы
                group.isAdmin(currentUser);

    }


    private boolean canModifyAd(User currentUser, Ad ad) {
        // Владелец рекламы
        if (ad.getOwner().getId().equals(currentUser.getId())) {
            return true;
        }


        // Проверяем права в группе, если реклама привязана к группе
        if (ad.getOwningGroup() != null) {
            return ad.getOwningGroup().isOwner(currentUser) ||
                    ad.getOwningGroup().isAdmin(currentUser);
        }

        return false;
    }

    @Override
    public void deleteAd(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new AdNotFoundException(adId));

        if (ad.getStatus() == Ad.AdStatus.ACTIVE) {
            throw new IllegalStateException("Cannot delete active advertisement");
        }

        adRepository.delete(ad);
        log.info("Advertisement {} has been deleted", adId);
    }

    @Override
    public AdDTO getAd(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new AdNotFoundException(adId));
        return adMapper.toDTO(ad);
    }

    @Override
    public List<AdDTO> getActiveAds() {
        List<Ad> activeAds = adRepository.findByStatusAndIsActiveTrue(Ad.AdStatus.ACTIVE);
        return activeAds.stream()
                .map(adMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdDTO> getAdsByOwner(Long ownerId) {
        List<Ad> ownerAds = adRepository.findByOwnerIdAndStatus(ownerId, Ad.AdStatus.ACTIVE);
        return ownerAds.stream()
                .map(adMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdDTO> getAdsByGroup(Long groupId) {
        List<Ad> groupAds = adRepository.findByOwningGroupIdAndStatus(groupId, Ad.AdStatus.ACTIVE);
        return groupAds.stream()
                .map(adMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void activateAd(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new AdNotFoundException(adId));

        // Проверяем, что статус DRAFT
        if (ad.getStatus() != Ad.AdStatus.DRAFT) {
            throw new IllegalStateException("Можно активировать только рекламу в статусе DRAFT");
        }

        validateAdForActivation(ad);

        // Меняем статус на ACTIVE и устанавливаем флаг активности
        ad.setStatus(Ad.AdStatus.ACTIVE);
        ad.setActive(true);

        adRepository.save(ad);
        log.info("Advertisement {} has been activated", adId);
    }

    private void validateAdForActivation(Ad ad) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(ad.getStartTime())) {
            throw new IllegalStateException(
                    "Нельзя активировать рекламу до времени начала показа");
        }

        if (now.isAfter(ad.getEndTime())) {
            throw new IllegalStateException(
                    "Нельзя активировать рекламу после времени окончания показа");
        }

        if (ad.getSchedules().isEmpty()) {
            throw new IllegalStateException(
                    "У рекламы должно быть хотя бы одно расписание");
        }
    }

    @Override
    public void deactivateAd(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new AdNotFoundException(adId));

        log.info("Deactivating ad {}: current status={}, active={}",
                adId, ad.getStatus(), ad.isActive());

        // Устанавливаем неактивное состояние
        ad.setActive(false);

        // Если реклама была активна, переводим её в статус PAUSED
        if (ad.getStatus() == Ad.AdStatus.ACTIVE) {
            ad.setStatus(Ad.AdStatus.PAUSED);
        }

        adRepository.save(ad);
        log.info("Ad {} has been deactivated: new status={}, active={}",
                adId, ad.getStatus(), ad.isActive());
    }

    @Override
    @Scheduled(fixedRate = 60000) // Каждую минуту
    public void processScheduledAds() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek currentDay = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime();

        List<Ad> activeAds = adRepository.findActiveAdsBySchedule(
                Ad.AdStatus.ACTIVE, now, currentDay, currentTime);

        for (Ad ad : activeAds) {
            processActiveAd(ad, now);
        }
    }

    @Override
    public List<AdDTO> getActiveAdsByGroup(Long groupId) {
        // Сначала проверим все рекламы группы
        List<Ad> allAds = adRepository.findAllByGroupId(groupId);
        log.info("Found {} total ads for group {}", allAds.size(), groupId);

        // Теперь получим только активные
        List<Ad> activeAds = adRepository.findByOwningGroupIdAndStatusAndIsActiveTrue(
                groupId, Ad.AdStatus.ACTIVE);
        log.info("Found {} active ads for group {}", activeAds.size(), groupId);

        // Преобразуем в DTO
        List<AdDTO> adDTOs = activeAds.stream()
                .map(adMapper::toDTO)
                .collect(Collectors.toList());

        log.info("Ads status check for group {}:", groupId);
        allAds.forEach(ad -> {
            log.info("Ad {}: status={}, active={}, startTime={}, endTime={}",
                    ad.getId(),
                    ad.getStatus(),
                    ad.isActive(),
                    ad.getStartTime(),
                    ad.getEndTime()
            );
        });

        return adDTOs;
    }


    private void processActiveAd(Ad ad, LocalDateTime now) {
        log.info("Processing ad ID: {}, status: {}, active: {}, startTime: {}, endTime: {}",
                ad.getId(), ad.getStatus(), ad.isActive(), ad.getStartTime(), ad.getEndTime());

        // Проверяем временные рамки
        if (now.isBefore(ad.getStartTime()) || now.isAfter(ad.getEndTime())) {
            log.info("Ad {} is outside time range (now: {})", ad.getId(), now);
            deactivateAd(ad.getId());
            return;
        }

        // Проверяем статус
        if (ad.getStatus() != Ad.AdStatus.ACTIVE && ad.getStatus() != Ad.AdStatus.DRAFT) {
            log.info("Ad {} is not in ACTIVE or DRAFT status, skipping schedule check", ad.getId());
            return;
        }

        // Проверяем расписание
        DayOfWeek currentDay = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime();

        log.info("Checking schedule for ad {}: day={}, time={}",
                ad.getId(), currentDay, currentTime);

        boolean isScheduled = ad.getSchedules().stream()
                .anyMatch(schedule -> {
                    boolean matches = schedule.getDayOfWeek() == currentDay &&
                            !currentTime.isBefore(schedule.getStartTime()) &&
                            !currentTime.isAfter(schedule.getEndTime());

                    if (matches) {
                        log.info("Found matching schedule for ad {}: day={}, start={}, end={}",
                                ad.getId(), schedule.getDayOfWeek(),
                                schedule.getStartTime(), schedule.getEndTime());
                    }

                    return matches;
                });

        if (!isScheduled && ad.isActive()) {
            log.info("Deactivating ad {} because it's not scheduled for current time", ad.getId());
            deactivateAd(ad.getId());
        } else if (isScheduled && ad.getStatus() == Ad.AdStatus.ACTIVE && !ad.isActive()) {
            log.info("Activating ad {} because it's scheduled for current time", ad.getId());
            activateAd(ad.getId());
        }

        // Логируем итоговое состояние
        log.info("Finished processing ad {}: status={}, active={}",
                ad.getId(), ad.getStatus(), ad.isActive());
    }

    @Override
    public List<AdDTO> getAllAdsByOwner(Long ownerId) {
        return adRepository.findAllByOwnerId(ownerId)
                .stream()
                .map(adMapper::toDTO)
                .collect(Collectors.toList());
    }


}