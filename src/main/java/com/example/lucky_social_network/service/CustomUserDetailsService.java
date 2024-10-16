package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.Admin;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.AdminRepository;
import com.example.lucky_social_network.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Attempting to load user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });
        log.info("User found: {}", user);

        Admin admin = adminRepository.findByUser(user).orElse(null);
        if (admin != null) {
            log.info("User {} is an admin with role: {}", username, admin.getRole());
        }

        return new CustomUserDetails(user, admin);
    }
}


//package com.example.lucky_social_network.service;
//
//import com.example.lucky_social_network.model.Admin;
//import com.example.lucky_social_network.model.User;
//import com.example.lucky_social_network.repository.AdminRepository;
//import com.example.lucky_social_network.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private  final  UserRepository userRepository;
//
//    @Transactional
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        log.info("Attempting to load user: {}", username);
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> {
//                    log.error("User not found: {}", username);
//                    return new UsernameNotFoundException("User not found with username: " + username);
//                });
//        log.info("User found: {}", user);
//        return new CustomUserDetails(user);
//    }
//}