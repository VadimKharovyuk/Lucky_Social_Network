package com.example.lucky_social_network.service;

import com.example.lucky_social_network.model.Admin;
import com.example.lucky_social_network.model.User;
import com.example.lucky_social_network.repository.AdminRepository;
import com.example.lucky_social_network.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;

    @Transactional
    public Admin convertUserToAdmin(Long userId, Admin.AdminRole adminRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Проверяем, не является ли пользователь уже администратором
        if (adminRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("User is already an admin");
        }

        // Создаем нового администратора
        Admin admin = new Admin();
        admin.setUser(user);
        admin.setRole(adminRole);
        admin.setTwoFactorSecret(generateTwoFactorSecret());
        admin.setLastLogin(LocalDateTime.now());

        // Сохраняем администратора
        Admin savedAdmin = adminRepository.save(admin);

        // Добавляем роль ADMIN пользователю
        user.addRole(User.Role.ADMIN);
        userRepository.save(user);

        return savedAdmin;
    }

    private String generateTwoFactorSecret() {
        Random random = new Random();
        int secretNumber = random.nextInt(10000);
        return String.format("%04d", secretNumber);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @Transactional
    public void updateAdminRole(Long adminId, Admin.AdminRole newRole) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with id: " + adminId));

        // Проверяем, изменилась ли роль
        if (admin.getRole() != newRole) {
            admin.setRole(newRole);
            adminRepository.save(admin);

            // Обновляем роль пользователя, если это необходимо
            User user = admin.getUser();
            User.Role userRole = Admin.mapAdminRoleToUserRole(newRole);
            if (!user.getRoles().contains(userRole)) {
                user.getRoles().clear();
                user.addRole(userRole);
                userRepository.save(user);
            }

            // Здесь можно добавить логирование изменения роли
            log.info("Updated admin role for admin id: {}. New role: {}", adminId, newRole);
        }
    }

    public void removeAdmin(Long adminId) {
        adminRepository.deleteById(adminId);
    }
}