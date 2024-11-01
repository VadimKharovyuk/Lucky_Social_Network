package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.management.relation.Role;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User>findByUsername(String username);

    Optional<User>findById(Long id);

    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByLastNameOrFirstNameOrUsernameContainingIgnoreCase(@Param("searchTerm") String searchTerm);


    @Query("SELECT u.roles FROM User u WHERE u.id = :userId")
    Set<Role> findRolesById(@Param("userId") Long userId);


    Optional<User> findByEmail(String email);

    @Query("SELECT u.lastLogin FROM User u WHERE u.id = :userId")
    Optional<LocalDateTime> findLastLoginById(@Param("userId") Long userId);

    boolean existsByEmail(String email);

    // Можно также добавить более точный метод, исключающий текущего пользователя
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :userId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") Long userId);


}
