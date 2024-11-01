package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByUserIdOrderByStartDateDesc(Long userId);

    List<Education> findByUserIdAndEducationType(Long userId, Education.EducationType type);

    boolean existsByUserIdAndEducationTypeAndCurrentTrue(Long userId, Education.EducationType type);

    @Query("SELECT e FROM Education e WHERE e.user.id = :userId " +
            "AND ((e.startDate BETWEEN :startDate AND :endDate) " +
            "OR (e.endDate BETWEEN :startDate AND :endDate))")
    List<Education> findByUserIdAndPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    Optional<Education> findByIdAndUserId(Long id, Long userId);
}
