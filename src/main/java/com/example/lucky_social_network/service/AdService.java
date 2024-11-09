package com.example.lucky_social_network.service;

import com.example.lucky_social_network.dto.AdCreateRequest;
import com.example.lucky_social_network.dto.AdDTO;
import com.example.lucky_social_network.dto.AdUpdateRequest;
import com.example.lucky_social_network.model.User;

import java.nio.file.AccessDeniedException;
import java.util.List;


public interface AdService {

    AdDTO createAd(AdCreateRequest request, User owner) throws AccessDeniedException;

    AdDTO updateAd(Long adId, AdUpdateRequest request);

    AdDTO updateAd(Long adId, AdUpdateRequest request, User currentUser) throws AccessDeniedException;

    void deleteAd(Long adId);

    AdDTO getAd(Long adId);

    List<AdDTO> getActiveAds();

    List<AdDTO> getAdsByOwner(Long ownerId);

    List<AdDTO> getAdsByGroup(Long groupId);

    void activateAd(Long adId);

    void deactivateAd(Long adId);

    void processScheduledAds();

    List<AdDTO> getActiveAdsByGroup(Long groupId);

    List<AdDTO> getAllAdsByOwner(Long ownerId);
}
