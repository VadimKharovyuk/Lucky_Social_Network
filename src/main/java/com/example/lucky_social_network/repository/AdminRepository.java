package com.example.lucky_social_network.repository;

import com.example.lucky_social_network.model.Admin;
import com.example.lucky_social_network.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {


    Optional<Admin> findByUser(User user);

}
