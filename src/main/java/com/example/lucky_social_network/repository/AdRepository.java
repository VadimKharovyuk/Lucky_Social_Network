package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Ad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long> {
    List<Ad> findByStatusAndIsActiveTrue(Ad.AdStatus status);

    List<Ad> findByOwnerIdAndStatus(Long ownerId, Ad.AdStatus status);

    List<Ad> findByOwningGroupIdAndStatus(Long groupId, Ad.AdStatus status);

    @Query("SELECT a FROM Ad a WHERE a.status = :status " +
            "AND a.startTime <= :now AND a.endTime >= :now " +
            "AND EXISTS (SELECT s FROM a.schedules s WHERE s.dayOfWeek = :dayOfWeek " +
            "AND s.startTime <= :timeNow AND s.endTime >= :timeNow)")
    List<Ad> findActiveAdsBySchedule(
            @Param("status") Ad.AdStatus status,
            @Param("now") LocalDateTime now,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("timeNow") LocalTime timeNow
    );


    List<Ad> findByOwningGroupIdAndStatusAndIsActiveTrue(Long groupId, Ad.AdStatus status);

    // Добавьте метод для отладки
    @Query("SELECT a FROM Ad a WHERE a.owningGroup.id = :groupId")
    List<Ad> findAllByGroupId(@Param("groupId") Long groupId);


    @Query("SELECT a FROM Ad a WHERE a.owner.id = :ownerId ORDER BY a.createdAt DESC")
    List<Ad> findAllByOwnerId(@Param("ownerId") Long ownerId);

}
