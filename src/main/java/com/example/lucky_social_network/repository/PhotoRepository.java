package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByAlbumIdOrderByUploadedAtDesc(Long albumId);

    // Или без сортировки
    // List<Photo> findByAlbumId(Long albumId);

    // Можно добавить вариант с пагинацией
    // Page<Photo> findByAlbumId(Long albumId, Pageable pageable);

}
