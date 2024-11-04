package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.WorkExperience;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE;

public interface WorkExperienceRepository extends JpaRepository<WorkExperience, Long> {
    @QueryHints(@QueryHint(name = HINT_FETCH_SIZE, value = "20"))
    @Query("SELECT w FROM WorkExperience w " +
            "WHERE w.user.id = :userId " +
            "ORDER BY w.startDate DESC")
    List<WorkExperience> findByUserIdOrderByStartDateDesc(Long userId);

    @QueryHints(@QueryHint(name = HINT_FETCH_SIZE, value = "20"))
    @Query("SELECT w FROM WorkExperience w " +
            "LEFT JOIN FETCH w.user " +  // Используем LEFT JOIN FETCH
            "WHERE w.id = :id AND w.user.id = :userId")
    Optional<WorkExperience> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndCurrentTrue(Long userId);

    @Query("SELECT w FROM WorkExperience w WHERE w.user.id = :userId " +
            "AND ((w.startDate BETWEEN :startDate AND :endDate) " +
            "OR (w.endDate BETWEEN :startDate AND :endDate))")
    List<WorkExperience> findByUserIdAndPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
