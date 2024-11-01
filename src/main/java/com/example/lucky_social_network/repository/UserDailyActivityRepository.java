package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.model.UserDailyActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDailyActivityRepository extends JpaRepository<UserDailyActivity, Long> {

    @Query("SELECT uda FROM UserDailyActivity uda " +
            "WHERE uda.user = :user AND uda.date = :date")
    Optional<UserDailyActivity> findByUserAndDate(@Param("user") User user,
                                                  @Param("date") LocalDate date);

    @Query("SELECT uda FROM UserDailyActivity uda " +
            "WHERE uda.user = :user " +
            "ORDER BY uda.date DESC")
    List<UserDailyActivity> findByUserOrderByDateDesc(@Param("user") User user);

    @Query("SELECT COUNT(uda) > 0 FROM UserDailyActivity uda " +
            "WHERE uda.user = :user AND uda.date = :date")
    boolean existsByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);

    @Query("SELECT uda FROM UserDailyActivity uda " +
            "WHERE uda.user = :user " +
            "ORDER BY uda.firstActivity ASC " +
            "LIMIT 1")
    Optional<UserDailyActivity> findFirstByUserOrderByFirstActivityAsc(@Param("user") User user);

    @Query("SELECT uda FROM UserDailyActivity uda " +
            "WHERE uda.user = :user " +
            "ORDER BY uda.lastActivity DESC " +
            "LIMIT 1")
    Optional<UserDailyActivity> findFirstByUserOrderByLastActivityDesc(@Param("user") User user);
}
