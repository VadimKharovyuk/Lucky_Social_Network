package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    @EntityGraph(attributePaths = {"photos", "owner"})
    Page<Album> findByOwnerId(Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"photos", "owner"})
    List<Album> findByOwnerId(Long ownerId);

    @EntityGraph(attributePaths = {"photos", "owner"})
    @Query("SELECT a FROM Album a WHERE a.owner.id = :ownerId AND a.isPrivate = false")
    Page<Album> findPublicAlbums(@Param("ownerId") Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"photos", "owner"})
    Page<Album> findByTitleContainingAndIsPrivateFalse(String title, Pageable pageable);

    @EntityGraph(attributePaths = {"photos", "owner"})
    Page<Album> findByOwnerIdOrderByCreatedAtDesc(Long ownerId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"photos", "owner"})
    Optional<Album> findById(Long id);

    // Добавим ещё полезные методы

    @EntityGraph(attributePaths = {"photos", "owner"})
    @Query("SELECT a FROM Album a WHERE a.owner.id = :ownerId " +
            "AND (LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Album> searchAlbums(@Param("ownerId") Long ownerId,
                             @Param("search") String search,
                             Pageable pageable);

    @Query("SELECT COUNT(p) FROM Album a JOIN a.photos p WHERE a.id = :albumId")
    long countPhotosByAlbumId(@Param("albumId") Long albumId);
}
