package com.example.lucky_social_network.service;

import com.example.lucky_social_network.exception.ResourceNotFoundException;
import com.example.lucky_social_network.model.Album;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.model.UserActivityEvent;
import com.example.lucky_social_network.repository.AlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class AlbumService {
    private final AlbumRepository albumRepository;
    public final ActivityPublisher activityPublisher;


    // Создание нового альбома
    public Album createAlbum(String title, String description, Boolean isPrivate, User owner) {
        Album album = new Album();
        album.setTitle(title);
        album.setDescription(description);
        album.setIsPrivate(isPrivate);
        album.setCreatedAt(LocalDateTime.now());
        album.setOwner(owner);
        album.setPhotoCount(0);
        activityPublisher.publishActivity(owner.getId(), UserActivityEvent.ActivityType.PHOTO_UPLOADED);

        return albumRepository.save(album);
    }

    // Получение альбома по ID с проверкой доступа
    public Album getAlbumById(Long albumId, User currentUser) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found with id: " + albumId));

        // Проверка доступа к приватному альбому
        if (album.getIsPrivate() && !album.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have access to this album");
        }
        activityPublisher.publishActivity(currentUser.getId(), UserActivityEvent.ActivityType.PHOTO_UPLOADED);

        return album;
    }

    // Получение всех публичных альбомов пользователя
    public Page<Album> getPublicAlbumsByUserId(Long userId, Pageable pageable) {
        return albumRepository.findByOwnerIdAndIsPrivateFalse(userId, pageable);
    }

    // Получение всех альбомов пользователя (для владельца)
    public Page<Album> getAllUserAlbums(Long userId, User currentUser, Pageable pageable) {
        if (!userId.equals(currentUser.getId())) {
            return getPublicAlbumsByUserId(userId, pageable);
        }
        return albumRepository.findByOwnerId(userId, pageable);
    }


    // Обновление альбома
    public Album updateAlbum(Long albumId, String title, String description, Boolean isPrivate, User currentUser) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found with id: " + albumId));

        if (!album.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to update this album");
        }

        if (title != null && !title.trim().isEmpty()) {
            album.setTitle(title);
        }
        if (description != null) {
            album.setDescription(description);
        }
        if (isPrivate != null) {
            album.setIsPrivate(isPrivate);
        }

        return albumRepository.save(album);
    }

    // Удаление альбома
    public void deleteAlbum(Long albumId, User currentUser) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found with id: " + albumId));

        if (!album.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to delete this album");
        }

        albumRepository.delete(album);
    }

    // Поиск альбомов по названию (только публичные)
    public Page<Album> searchAlbumsByTitle(String title, Pageable pageable) {
        return albumRepository.findByTitleContainingAndIsPrivateFalse(title, pageable);
    }

    // Проверка, является ли пользователь владельцем альбома
    public boolean isAlbumOwner(Long albumId, User user) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found with id: " + albumId));
        return album.getOwner().getId().equals(user.getId());
    }

    // Получение общего количества фотографий во всех альбомах пользователя
    public long getTotalPhotosCount(Long userId) {
        return albumRepository.findByOwnerId(userId).stream()
                .mapToInt(Album::getPhotoCount)
                .sum();
    }

    // Получение последних созданных альбомов пользователя
    public List<Album> getRecentAlbums(Long userId, int limit) {
        return albumRepository.findByOwnerIdOrderByCreatedAtDesc(userId, Pageable.ofSize(limit)).getContent();
    }
}