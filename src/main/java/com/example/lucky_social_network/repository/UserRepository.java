package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User>findByUsername(String username);

    Optional<User>findById(Long id);

    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByLastNameOrFirstNameOrUsernameContainingIgnoreCase(@Param("searchTerm") String searchTerm);


}
