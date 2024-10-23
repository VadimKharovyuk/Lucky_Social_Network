package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {


    Page<Album> findByOwnerId(Long ownerId, Pageable pageable);

    List<Album> findByOwnerId(Long ownerId);

    Page<Album> findByOwnerIdAndIsPrivateFalse(Long ownerId, Pageable pageable);

    Page<Album> findByTitleContainingAndIsPrivateFalse(String title, Pageable pageable);

    Page<Album> findByOwnerIdOrderByCreatedAtDesc(Long ownerId, Pageable pageable);

}
