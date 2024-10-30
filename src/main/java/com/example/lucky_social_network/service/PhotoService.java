package com.example.lucky_social_network.service;


import com.example.lucky_social_network.exception.ResourceNotFoundException;
import com.example.lucky_social_network.model.Album;
import com.example.lucky_social_network.model.Photo;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.model.UserActivityEvent;
import com.example.lucky_social_network.repository.AlbumRepository;
import com.example.lucky_social_network.repository.PhotoRepository;
import com.example.lucky_social_network.service.picService.ImgurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PhotoService {
    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;
    private final ImgurService imgurService;
    private final ActivityPublisher activityPublisher;


    public Photo addPhotoToAlbum(byte[] imageData, String description, Long albumId, User user) throws AccessDeniedException {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found"));

        if (!album.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to add photos to this album");
        }

        // Загружаем фото через ImgurService
        String photoUrl = imgurService.uploadImage(imageData);
        if (photoUrl == null) {
            throw new RuntimeException("Failed to upload image to Imgur");
        }

        Photo photo = new Photo();
        photo.setPhotoUrl(photoUrl);
        photo.setDescription(description);
        photo.setUploadedAt(LocalDateTime.now());
        photo.setAlbum(album);
        photo.setAuthor(user);
        photo.setIsPrivate(album.getIsPrivate());

        // Увеличиваем счетчик фотографий в альбоме
        album.setPhotoCount(album.getPhotoCount() + 1);
        albumRepository.save(album);

        return photoRepository.save(photo);
    }

    // Добавим метод для массовой загрузки фотографий
    public List<Photo> addPhotosToAlbum(List<byte[]> imagesData, List<String> descriptions, Long albumId, User user) throws AccessDeniedException {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found"));

        if (!album.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to add photos to this album");
        }

        List<Photo> uploadedPhotos = new ArrayList<>();

        for (int i = 0; i < imagesData.size(); i++) {
            String description = descriptions != null && descriptions.size() > i ? descriptions.get(i) : null;

            // Загружаем каждое фото через ImgurService
            String photoUrl = imgurService.uploadImage(imagesData.get(i));
            if (photoUrl == null) {
                // Логируем ошибку и продолжаем с следующим фото
                continue;
            }

            Photo photo = new Photo();
            photo.setPhotoUrl(photoUrl);
            photo.setDescription(description);
            photo.setUploadedAt(LocalDateTime.now());
            photo.setAlbum(album);
            photo.setAuthor(user);
            photo.setIsPrivate(album.getIsPrivate());

            uploadedPhotos.add(photoRepository.save(photo));
        }

        // Обновляем счетчик фотографий в альбоме
        album.setPhotoCount(album.getPhotoCount() + uploadedPhotos.size());
        albumRepository.save(album);

        return uploadedPhotos;
    }

    public void removePhotoFromAlbum(Long photoId, User user) throws AccessDeniedException {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found"));

        if (!photo.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to remove this photo");
        }

        Album album = photo.getAlbum();
        album.setPhotoCount(Math.max(0, album.getPhotoCount() - 1));
        albumRepository.save(album);

        photoRepository.delete(photo);
    }

    public Photo updatePhoto(Long photoId, String description, Boolean isPrivate, User user) throws AccessDeniedException {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found"));

        if (!photo.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to update this photo");
        }

        if (description != null) photo.setDescription(description);
        if (isPrivate != null) photo.setIsPrivate(isPrivate);

        return photoRepository.save(photo);
    }

    public List<Photo> getPhotosByAlbumId(Long albumId) {
        List<Photo> listPhoto = photoRepository.findByAlbumIdOrderByUploadedAtDesc(albumId);
        return listPhoto;
    }


    public Photo getPhotoById(Long photoId, User currentUser) throws AccessDeniedException {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

        // Проверяем права доступа
        if (photo.getIsPrivate() && !photo.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to view this photo");
        }
        activityPublisher.publishActivity(currentUser.getId(), UserActivityEvent.ActivityType.PROFILE_UPDATED);

        return photo;
    }
}

